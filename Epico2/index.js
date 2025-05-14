const express = require('express');
const dotenv = require('dotenv');
const morgan = require('morgan');
const appRoutes = require('./src/routes/appRoutes');


// Carregar variáveis de ambiente
dotenv.config();

if (!process.env.JWT_SECRET) {
  console.error('JWT_SECRET is not defined in .env file');
  process.exit(1);
}

const app = express();

// Middleware
app.use(express.json());
app.use(morgan('dev'));
app.use('/api', appRoutes);

// Validação de Health Check
app.get('/health', (req, res) => {
  res.status(200).json({ 
    status: 'UP',
    timestamp: new Date().toISOString()
  });
});

app.use((err, req, res, next) => {
    console.error('Global error handler:', err);
    res.status(500).json({ error: 'Internal server error' });
});

// Porta
const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
  console.log(`Server is running at port ${PORT}`);
});