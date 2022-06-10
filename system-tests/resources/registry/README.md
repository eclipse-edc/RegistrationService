This directory contains JSON files with the registry participants.
The files are used by the RegistrationService.

This directory is provided by default in the docker-compose file and used in system tests.

To use file based implementation of registry service make sure to set following environment variables:

- `NODES_JSON_DIR`: path to registry directory
- `NODES_JSON_FILES_PREFIX`: prefix for all file names in registry directory that contain the participants (`registry-` in this directory).
