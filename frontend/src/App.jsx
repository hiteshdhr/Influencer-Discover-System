import { useState } from 'react';
import { Routes, Route } from 'react-router-dom';
import Navbar from './components/Navbar';
import HeroSection from './components/HeroSection';
import DiscoveryForm from './components/DiscoveryForm';
import CreatorCard from './components/CreatorCard';
import StatsRow from './components/StatsRow';
import { ErrorAlert, SkeletonLoading, EmptyState } from './components/UIHelpers';

function DiscoverPage() {
  const [keywords, setKeywords] = useState('react, web dev, frontend');
  const [brandContext, setBrandContext] = useState('Ed-tech platform helping students master frontend development');
  const [loading, setLoading] = useState(false);
  const [results, setResults] = useState(null);
  const [error, setError] = useState(null);

  const handleDiscover = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    setResults(null);

    try {
      const keywordArray = keywords.split(',').map(k => k.trim()).filter(Boolean);
      const API_BASE = "http://localhost:5005";
      const response = await fetch(`${API_BASE}/api/discover`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ keywords: keywordArray, brandContext }),
      });

      const data = await response.json();
      
      if (!response.ok) {
        throw new Error(data.error || data.message || `HTTP error! status: ${response.status}`);
      }
      
      setResults(data.results || data.creators || []);
    } catch (err) {
      console.error(err);
      setError(err.message || 'Failed to connect to the backend API. Please ensure the server is running.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <main className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 py-16">
      <HeroSection />
      
      <DiscoveryForm
        keywords={keywords}
        setKeywords={setKeywords}
        brandContext={brandContext}
        setBrandContext={setBrandContext}
        loading={loading}
        onSubmit={handleDiscover}
      />

      <ErrorAlert error={error} />
      
      {loading && <SkeletonLoading />}
      
      {results && !loading && (
        <div className="space-y-8">
          <StatsRow results={results} />
          
          {results.length > 0 ? (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              {results.map((creator, i) => (
                <CreatorCard key={creator.creatorId || i} creator={creator} />
              ))}
            </div>
          ) : (
            <EmptyState />
          )}
        </div>
      )}
    </main>
  );
}

function DummyPage({ title }) {
  return (
    <div className="flex items-center justify-center h-[60vh]">
      <h1 className="text-3xl font-bold text-zinc-500">{title}</h1>
    </div>
  );
}

function App() {
  return (
    <div className="min-h-screen bg-zinc-950 text-zinc-100 font-sans">
      <Navbar />
      <Routes>
        <Route path="/" element={<DummyPage title="Dashboard (Coming Soon)" />} />
        <Route path="/discover" element={<DiscoverPage />} />
        <Route path="/pricing" element={<DummyPage title="Pricing" />} />
      </Routes>
    </div>
  );
}

export default App;
