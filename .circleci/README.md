# How to make changes?
##### Install the CircleCI CLI:
https://circleci.com/docs/2.0/local-cli/#installation

##### Making a change
Change the areas of the .circleci/config.yml file that need to be edited

##### To verify your changes
Any config can be verified, to ensure your changes are valid against the yaml and orb schemas, 
from the root of the project, run: `circleci config validate .circleci/config.yml --org-slug gh/gresham-computing --token $CIRCLE_TOKEN`

##### Possible errors:
- Your file must be encoded in UTF-8 (powershell defaulted to UTF-16)
- Must use Unix style line endings (LF, not CRLF)
