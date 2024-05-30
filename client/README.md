# Client (Web)

:hammer_and_wrench: **Development**
- Do the [npm link](https://docs.npmjs.com/cli/v10/commands/npm-link) between the projects:
  - On pbc-notify-ui: ```npm i``` and ```npm link```
  - On client app: ```npm i``` and ```npm link @genesislcap/pbc-notify-ui```
- On pbc-notify-ui [package.json](package.json) change the following lines:
  - ```"main": "dist/esm/index.js",``` to ```"main": "src/index.ts",```
  - ```"default": "./dist/esm/index.js"``` to ```"default": "./src/index.ts"```
- :warning: Always do a ```git rollback``` on the ```package.json``` before a commit or just don't commit it

:package: **Build**
- The recommended build command to be used is ```genx build -b ts```
- Undo the following lines on pbc-notify-ui [package.json] (package.json):
  - ```"main": "src/index.ts",``` to ```"main": "dist/esm/index.js",```
  - ```"default": "./src/index.ts"``` to ```"default": "./dist/esm/index.js"```
- Do a ```npm run build``` on pbc-notify-ui
- Do a ```npm run build``` on client app
- A ```dist``` folder will be generated with the front-end
