const AWS = require('aws-sdk');
const docClient = new AWS.DynamoDB.DocumentClient();
const { Configuration, OpenAIApi } = require('openai');
const dotenv = require('dotenv');
const uuid = require('uuid');

dotenv.config();

const openai = new OpenAIApi(
  new Configuration({ apiKey: 'api_key' })
);

let conversations = {};

function countTokens(text) {
    return text.split(' ').length;
}

exports.handler = async (event, context) => {
    let body;

    if (typeof event.body === 'string') {
        body = JSON.parse(event.body);
    } else {
        body = event.body;
    }

    let user_message = body.user_message || '';
    let session_id = body.session_id || '';
    let username = body.username || '';

    if (!user_message || !username) {
        return {statusCode: 400, body: JSON.stringify({error: 'Missing user_message or username'})};
    }

    if (!session_id) {
        session_id = uuid.v4();
    }

    let max_user_tokens = 200;
    let max_assistant_tokens = 50;

    if (countTokens(user_message) > max_user_tokens) {
        return {statusCode: 400, body: JSON.stringify({error: 'User message too long'})};
    }

    // Check if user has available messages
    const params = {
        TableName: 'User-5dozx2apmza3zbqrnwjxz33f7i-dev',
        Key: {
            id: username
        }
    };

    try {
        const data = await docClient.get(params).promise();
        if (!data.Item || data.Item.amount <= 0) {
            return {
                statusCode: 200,
                body: JSON.stringify({
                    assistant_message: "Your attempts are over for today. Please try again tomorrow.",
                    session_id: session_id
                })
            };
        }
    } catch (err) {
        console.log('DynamoDB read error: ', err);
        return {statusCode: 500, body: JSON.stringify({error: `Unexpected error: ${err}`})};
    }

    if (!(session_id in conversations)) {
        conversations[session_id] = [{role: 'system', content: 'Hello'}];
    }

    let conversation = conversations[session_id];
    conversation.push({role: 'user', content: user_message});

    try {
        let response = await openai.createChatCompletion({
            model: 'gpt-3.5-turbo',
            messages: conversation,
            temperature: 0.5,
            max_tokens: max_assistant_tokens
        });

        let assistant_message = response.data.choices[0].message.content.trim();
        conversation.push({role: 'assistant', content: assistant_message});

        // Subtract 1 from the amount field for the user
        const updateParams = {
            TableName: params.TableName,
            Key: {
                id: username
            },
            UpdateExpression: 'set amount = amount - :val',
            ExpressionAttributeValues: {
                ':val': 1
            }
        };

        await docClient.update(updateParams).promise();

        return {
            statusCode: 200,
            body: JSON.stringify({
                assistant_message: assistant_message,
                session_id: session_id
            })
        };
    } catch (e) {
        return {statusCode: 500, body: JSON.stringify({error: `Unexpected error: ${e}`})};
    }



};
