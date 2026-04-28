const express = require('express');
const cors = require('cors');
require('dotenv').config();

const discoveryRoutes = require('./routes/discoveryRoutes');

const app = express();
const PORT = process.env.PORT || 5000;

// Middleware
app.use(cors());
app.use(express.json());

// Routes
app.use('/api/discover', discoveryRoutes);

// Health check
app.get('/health', (req, res) => {
    res.json({ status: 'ok', message: 'Micro-Influencer Discovery API is running' });
});

app.listen(PORT, () => {
    console.log(`Server is running on port ${PORT}`);
});
