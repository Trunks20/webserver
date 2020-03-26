# HTTP Web Server
This project is an exercise proposed by the Network Departmen of the FIC (Faculty of Computer Science of the UDC, A Coruña) and consists of creating an HTTP/1.0 web server. This server supports the GET and HEAD methods, .txt, .png, .jpg, .html files (among others similar) and some basic HTTP errors. A configuration file is provided to specified some basic properties as port to listen, resources directory of default file. Two log files are provided too, where successfully or failure responses from the server are recorded.

The server has been only tested in Linux Mint 19.2. If any problem happens, try to run with sudo or contact via email: anxomm@gmail.com.

## Configurating the server
The configuration of the server is done by a file called 'config.properties' inside the root folder. The configuration file has the next fields:
- PORT: port where the server listens.
- DIRECTORY_INDEX: name of the default file.
- DIRECTORY: relative puth to the resources folder.
- ALLOW: when a folder is requested, the default file will be sent if allow is true and the file exists, the directory content will be listed if allow is true and the file does not exist, an 403 error (forbidden) will be prompt if allow is false and the default file does not exist.
- LOG_INDEX: relative path to the log files that record the requests.

**As DIRECTORY_INDEX as LOG_INDEX are relative paths from the configuration file 'config.properties'.**

## Running the server
Only the path to the configuration file is requested to execute the server. By default is at the root of the project, so an example of the path would be: /home/user/webserver/
**The path should end with '/'.**
