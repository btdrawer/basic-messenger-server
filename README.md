# basic-messenger-server

A basic messenger API built using Akka HTTP and PostgreSQL.

## Authentication

The API uses basic HTTP authentication that requires a user's username-password combination.

## Response format

Endpoints that either operate on, or retrieve, a single item have the following response format, if successful:

````JSON
{
  "success": true,
  "result": {},
  "message": "Item created successfully"
}
````

The `result` object is usually null for `PUT` and `DELETE` endpoint; the `message` is usually null for `GET` endpoints.

Endpoints that retrieve a list of items will simply return the list at the top level.

If an endpoint fails, this will be the response format (in this case, a user was not found):

````JSON
{
  "success": false,
  "result": null,
  "message": "User not found."
}
````

## Resources

### Server

#### Structure

| Parameter | Type             |
|-----------|------------------|
| id        | Int              |
| name      | String           |
| address   | String           |
| users     | ServerUserRole[] |
| messages  | Message[]        |

##### ServerUserRole

| Parameter | Type |
|-----------|------|
| user      | User |
| role      | Role |

##### Role

Every user on a server has one of these roles:

* `ADMIN` - Can send messages, add and remove users, update the server's name, and delete the server
* `MODERATOR` - Can send messages, add and remove users
* `MEMBER` - Can send messages

#### Sample JSON

````JSON
{
  "id": 1,
  "name": "Example Server",
  "address": "exampleserver",
  "users": [
    {
      "user": {
        "id": 1,
        "username": "ben",
        "status": "ONLINE"
      },
      "role": "ADMIN"
    }
  ],
  "messages": [
    {
      "id": 1,
      "content": "Hello",
      "user": {
        "id": 1,
        "username": "ben",
        "status": "ONLINE"
      },
      "createdAt": 16011364370000
    }
  ]
}
````

#### Endpoints

##### `POST /servers`

Create a new server.

###### Request body

| Parameter | Type   | Required? |
|-----------|--------|-----------|
| name      | String | Yes       |
| address   | String | Yes       |

###### Sample body

````JSON
{
  "name": "Example Server",
  "address": "exampleserver"
}
````

###### Sample response

Status: `200 OK`

````JSON
{
  "success": true,
  "result": {
    "id": 1,
    "name": "Example Server",
    "address": "exampleserver",
    "users": [
      {
        "user": {
          "id": 1,
          "username": "ben",
          "status": "ONLINE"
        },
        "role": "ADMIN"
      }
    ],
    "messages": []
  }
}
````

##### `GET /servers/search/<name>`

Search for servers by name.

###### Sample response

Status: `200 OK`

````JSON
[
  {
    "id": 1,
    "name": "Example Server",
    "address": "exampleserver"
  },
  {
    "id": 2,
    "name": "Another Server",
    "address": "anotherserver"
  }
]
````

##### `GET /servers/id/<id>`

Retrieve a single server by its ID.

###### Sample response

Status: `200 OK`

````JSON
{
  "success": true,
  "result": {
    "id": 1,
    "name": "Example Server",
    "address": "exampleserver",
    "users": [
      {
        "user": {
          "id": 1,
          "username": "ben",
          "status": "ONLINE" 
        },
        "role": "ADMIN" 
      }
    ],
    "messages": [
      {
        "id": 1,
        "content": "Hello",
        "user": {
          "id": 1,
          "username": "ben",
          "status": "ONLINE"
        },
        "createdAt": 16011364370000
      }
    ]
  },
  "message": null
}
````

##### `GET /servers/address/<address>`

Retrieve a single server by its address.

###### Sample response

Status: `200 OK`

````JSON
{
  "success": true,
  "result": {
    "id": 1,
    "name": "Example Server",
    "address": "exampleserver",
    "users": [
      {
        "user": {
          "id": 1,
          "username": "ben",
          "status": "ONLINE"
        },
        "role": "ADMIN"
      }
    ],
    "messages": [
      {
        "id": 1,
        "content": "Hello",
        "user": {
          "id": 1,
          "username": "ben",
          "status": "ONLINE"
        },
        "createdAt": 16011364370000
      }
    ]
  },
  "message": null
}
````

##### `PUT /servers/<id>`

Update a server.

###### Permissions level required

* `ADMIN`

###### Request body

| Parameter | Type   | Required? |
|-----------|--------|-----------|
| name      | String | No        |

Sample request:

````JSON
{
  "name": "Updated Server"
}
````

##### `PUT /servers/<server_id>/users/<user_id>`

Add a user to the server.

###### Permissions level required

* `ADMIN`
* `MODERATOR`

###### Sample response

Status: `200 OK`

````JSON
{
  "success": true,
  "result": null,
  "message": "User added to server."
}
````

##### `PUT /servers/<server_id>/users/<user_id>/roles/<role>`

Change the role of a user on the server.

###### Permissions level required

* `ADMIN`

###### Sample response

Status: `200 OK`

````JSON
{
  "success": true,
  "result": null,
  "message": "User role updated."
}
````

##### `DELETE /servers/<id>`

Delete a server.

###### Permissions level required

* `ADMIN`

###### Sample response

Status: `200 OK`

````JSON
{
  "success": true,
  "result": null,
  "message": "Server deleted successfully."
}
````

##### `DELETE /servers/<server_id>/users/<user_id>`

Remove a user from a server.


###### Permissions level required

* `ADMIN`
* `MODERATOR`

###### Sample response

Status: `200 OK`

````JSON
{
  "success": true,
  "result": null,
  "message": "User removed from server."
}
````

### User

#### Structure

| Parameter | Type             |
|-----------|------------------|
| id        | Int              |
| username  | String           |
| status    | Status           |
| servers   | UserServerRole[] |

##### Status

The `status` field must be one of the following values:

* `ONLINE`
* `OFFLINE`
* `BUSY`

##### UserServerRole

| Parameter | Type   |
|-----------|--------|
| server    | Server |
| role      | Role   |

##### PasswordReset

| Parameter | Type   |
|-----------|--------|
| question  | Int    |
| answer    | String |

The `question` is an ID number which should be provided by the server administrator.

#### Sample JSON

````JSON
{
  "id": 1,
  "username": "ben",
  "status": "ONLINE",
  "servers": [
    {
      "server": {
        "id": 1,
        "name": "Example Server",
        "address": "exampleserver"
      },
      "role": "ADMIN"
    }
  ]
}
````

#### Endpoints

##### `POST /users`

Create a new user (sign up).

| Parameter     | Type          | Required? |
|---------------|---------------|-----------|
| username      | String        | Yes       |
| password      | String        | Yes       |
| passwordReset | PasswordReset | Yes       |

**NOTE**: The password must contain at least one uppercase letter and one number.

###### Sample request

````JSON
{
  "username": "ben",
  "password": "Password222",
  "passwordReset": {
    "question": 1,
    "answer": "Hello"
  }
}
````

###### Sample response

Status: `200 OK`

````JSON
{
  "success": true,
  "result": {
    "id": 1,
    "username": "ben",
    "status": "ONLINE",
    "servers": []
  },
  "message": "User created successfully."
}
````

##### `GET /users/<id>`

Retrieve a user by their ID.

###### Sample response

Status: `200 OK`

````JSON
{
  "success": true,
  "result": {
    "id": 1,
    "username": "ben",
    "status": "ONLINE",
    "servers": [
      {
        "server": {
          "id": 1,
          "name": "Example Server",
          "address": "exampleserver"
        },
        "role": "ADMIN"
      }
    ]
  },
  "message": null
}
````

##### `PUT /users`

Update the authenticated user.

| Parameter     | Type          | Required? |
|---------------|---------------|-----------|
| username      | String        | No        |
| password      | String        | No        |
| status        | Status        | No        |
| passwordReset | PasswordReset | No        |

**NOTE**: The password must contain at least one uppercase letter and one number.

###### Sample request

````JSON
{
  "username": "ben",
  "password": "Password222",
  "status": "OFFLINE",
  "passwordReset": {
    "question": 1,
    "answer": "Hello"
  }
}
````

###### Sample response

Status: `200 OK`

````JSON
{
  "success": true,
  "result": null,
  "message": "User updated successfully."
}
````

##### `DELETE /users`

Delete the authenticated user.

###### Sample response

Status: `200 OK`

````JSON
{
  "success": true,
  "result": null,
  "message": "User deleted successfully."
}
````

### Message

#### Structure

| Parameter | Type             |
|-----------|------------------|
| id        | Int              |
| content   | String           |
| server    | Server           |
| sender    | ServerUserRole   |
| createdAt | Timestamp        |

#### Sample JSON

````JSON
{
  "id": 1,
  "content": "Hello",
  "server": {
    "id": 1,
    "name": "Example Server",
    "address": "exampleserver"
  },
  "sender": {
    "id": 1,
    "username": "ben",
    "status": "ONLINE"
  },
  "createdAt": 16011364370000
}
````

#### Endpoints

##### `POST /messages/server/<server_id>`

Send a message to a server.

###### Request body

| Parameter | Type   | Required? |
|-----------|--------|-----------|
| content   | String | Yes       |

###### Sample request

````JSON
{
  "content": "Hello"
}
````

###### Sample response

Status: `200 OK`

````JSON
{
  "id": 1,
  "content": "Hello",
  "server": {
    "id": 1,
    "name": "Example Server",
    "address": "exampleserver"
  },
  "sender": {
    "user": {
      "id": 1,
      "username": "ben",
      "status": "ONLINE"
    },
    "role": "ADMIN"
  },
  "createdAt": 16011364370000
}
````


##### `POST /messages/direct/<user_id>`

Send a message to another user.

###### Request body

| Parameter | Type   | Required? |
|-----------|--------|-----------|
| content   | String | Yes       |

###### Sample request

````JSON
{
  "content": "Hello"
}
````

###### Sample response

Status: `200 OK`

````JSON
{
  "id": 1,
  "content": "Hello",
  "server": {
    "id": 1,
    "name": "Example Server",
    "address": "exampleserver"
  },
  "sender": {
    "user": {
      "id": 1,
      "username": "ben",
      "status": "ONLINE"
    },
    "role": "ADMIN"
  },
  "createdAt": 16011364370000
}
````

##### `GET /messages/server/<server_id>`

Retrieve a list of messages from a server.

###### Query parameters

| Parameter | Type   | Required? | Default |
|-----------|--------|-----------|---------|
| limit     | Int    | No        | 100     |
| offset    | Int    | No        | 0       |

**NOTE**: The maximum `limit` that can be set is 1000.

###### Sample response

Status: `200 OK`

````JSON
[
  {
    "id": 1,
    "content": "Hello",
    "user": {
      "id": 1,
      "username": "ben",
      "status": "ONLINE"
    }
  }
]
````

##### `GET /messages/direct/<user_id>`

Retrieve a list of direct messages with another user.

###### Query parameters

| Parameter | Type   | Required? | Default |
|-----------|--------|-----------|---------|
| limit     | Int    | No        | 100     |
| offset    | Int    | No        | 0       |

**NOTE**: The maximum `limit` that can be set is 1000.

###### Sample response

Status: `200 OK`

````JSON
[
  {
    "id": 1,
    "content": "Hello",
    "user": {
      "id": 1,
      "username": "ben",
      "status": "ONLINE"
    }
  }
]
````

