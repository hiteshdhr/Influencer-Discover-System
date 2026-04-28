import { Loader2 } from 'lucide-react';

export default function DiscoveryForm({ keywords, setKeywords, brandContext, setBrandContext, loading, onSubmit }) {
  return (
    <div className="max-w-2xl mx-auto mb-16 bg-zinc-900 border border-zinc-800 rounded-xl p-6 md:p-8 shadow-sm">
      <form onSubmit={onSubmit} className="space-y-6">
        <div className="space-y-2">
          <label className="text-sm font-medium text-zinc-300">Niche Keywords</label>
          <input
            type="text"
            value={keywords}
            onChange={(e) => setKeywords(e.target.value)}
            placeholder="e.g. react, skincare, budget travel"
            required
            className="input-field"
          />
        </div>

        <div className="space-y-2">
          <label className="text-sm font-medium text-zinc-300">Brand Context & Campaign Goals</label>
          <textarea
            value={brandContext}
            onChange={(e) => setBrandContext(e.target.value)}
            rows="3"
            placeholder="Describe your brand, target audience, and collaboration goals..."
            required
            className="input-field resize-none"
          />
        </div>

        <button
          type="submit"
          disabled={loading}
          className="w-full flex items-center justify-center gap-2 bg-blue-600 hover:bg-blue-500 text-white font-medium py-2.5 px-4 rounded-md transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
        >
          {loading ? (
            <>
              <Loader2 className="w-4 h-4 animate-spin" />
              Analyzing...
            </>
          ) : (
            'Run Discovery'
          )}
        </button>
      </form>
    </div>
  );
}
