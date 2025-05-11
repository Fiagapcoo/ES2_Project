const apps = []; // Simulação de base de dados temporária

class App {
  constructor(appid, name, secretHashed, roles = []) {
    this.appid = appid;
    this.name = name;
    this.secret = secretHashed;
    this.roles = roles; // Por ex: ['admin'], ['client']
  }
}

module.exports = { App, apps };