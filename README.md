# HTTP Web Server
The server has been only tested in Linux Mint 19.2. If any problem happens, try to run with sudo.

## Configurating the server
The configuration of the server is done by a file called 'config.properties' inside the folder 'p1'. The configuration file has the next fields:
- PORT: port where the server listens.
- DIRECTORY_INDEX: name of the default file.
- DIRECTORY: relative puth to the resources folder.
- ALLOW: when a folder is requested, the default file will be sent if allow is true and the file exists, the directory content will be listed if allow is true and the file does not exist, an 403 error (forbidden) will be prompt if allow is false and the default file does not exist.
- LOG_INDEX: relative path to the log files that record the requests.

**As DIRECTORY_INDEX as LOG_INDEX are relative paths from the configuration file 'config.properties'.**

## Ejecución
Only the path to the configuration file is requested to execute the server. By default is at the root of the project, so an example of the path would be: /home/user/webserver/
**The path should end with '/'.**
