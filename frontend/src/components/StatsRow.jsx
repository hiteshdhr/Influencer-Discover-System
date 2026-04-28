import { Users, Target, BarChart3 } from 'lucide-react';

function StatCard({ icon: Icon, label, value }) {
  return (
    <div className="bg-zinc-900 border border-zinc-800 rounded-xl p-5 flex items-center gap-4">
      <div className="bg-zinc-800 p-3 rounded-lg text-zinc-400">
        <Icon className="w-5 h-5" />
      </div>
      <div>
        <p className="text-sm text-zinc-400 font-medium">{label}</p>
        <p className="text-xl font-bold text-zinc-100">{value}</p>
      </div>
    </div>
  );
}

export default function StatsRow({ results }) {
  const avgFit = Math.round(results.reduce((a, c) => a + (c.fitScore || 0), 0) / (results.length || 1));
  const topNiche = results[0]?.niche || 'Various';

  return (
    <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-8">
      <StatCard icon={Users} label="Creators Found" value={results.length} />
      <StatCard icon={Target} label="Avg Fit Score" value={`${avgFit}/100`} />
      <StatCard icon={BarChart3} label="Top Niche" value={topNiche} />
    </div>
  );
}
