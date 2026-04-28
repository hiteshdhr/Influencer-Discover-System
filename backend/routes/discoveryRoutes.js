const express = require('express');
const router = express.Router();
const discoveryController = require('../controllers/discoveryController');

// POST /api/discover
// Body: { "keywords": ["react", "web dev"], "brandContext": "Ed-tech platform for coders" }
router.post('/', discoveryController.runDiscovery);

module.exports = router;
