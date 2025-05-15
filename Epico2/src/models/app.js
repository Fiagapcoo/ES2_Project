// Temporary in-memory database simulation
const apps = []; // Array to hold registered app objects (simulates a database table)


// Class definition for an App object
class App {
  /**
   * Constructor to create a new App object
   * @param {string} appid - Unique identifier for the app
   * @param {string} name - Name of the application
   * @param {string} secretHashed - Hashed secret key used for authentication
   * @param {Array} roles - Array of roles assigned to the app (e.g., ['admin'], ['client'])
   * @param {string} passwordHashed - String representing the hashed password for the app
   */
  constructor(appid, name, secretHashed, roles = [], passwordHashed) {
    this.appid = appid;             // Unique ID for the app
    this.name = name;               // Application name
    this.secret = secretHashed;     // Hashed secret used for login/authentication
    this.roles = roles;             // Roles assigned to the app for permissions
    this.password = passwordHashed;
  }
}


// Exporting both the App class and the apps array
module.exports = { App, apps };
