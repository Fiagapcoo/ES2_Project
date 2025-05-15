// Importing core modules and libraries
const express = require('express');        // Express is a web framework for building APIs and handling HTTP requests/responses
const dotenv = require('dotenv');          // dotenv loads environment variables from a .env file into process.env
const morgan = require('morgan');          // Morgan is an HTTP request logger middleware for debugging

// Importing custom route handler
const appRoutes = require('./src/routes/appRoutes'); // Imports routes defined in a separate file

// Load environment variables from .env file
dotenv.config(); 

// Check if JWT_SECRET is set; exit if not
if (!process.env.JWT_SECRET) {
  console.error('JWT_SECRET is not defined in .env file');
  process.exit(1); // Stop server startup to avoid security issues
}

// Create Express app
const app = express();

// Apply middleware
app.use(express.json());        // Parses incoming requests with JSON payloads (e.g. from POST requests)
app.use(morgan('dev'));         // Logs HTTP requests to the console (method, URL, status, response time)
app.use('/api', appRoutes);     // Mounts the routes from appRoutes under the '/api' path

// Health check route for status monitoring (used by tools like Docker, Kubernetes, etc.)
app.get('/health', (req, res) => {
  res.status(200).json({ 
    status: 'UP',                      // Service is up and running
    timestamp: new Date().toISOString() // Current server time in ISO format
  });
});

// Global error handler middleware
app.use((err, req, res, next) => {
    console.error('Global error handler:', err); // Log the error to the console
    res.status(500).json({ error: 'Internal server error' }); // Send a generic error response
});

// Define the port to listen on
const PORT = process.env.PORT || 3000; // Use environment variable or default to 3000
app.listen(PORT, () => {
  console.log(`Server is running at port ${PORT}`); // Confirmation message when the server starts
});