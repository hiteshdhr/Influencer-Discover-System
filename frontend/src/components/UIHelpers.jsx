import { AlertCircle } from 'lucide-react';

export function ErrorAlert({ error }) {
  if (!error) return null;
  return (
    <div className="max-w-2xl mx-auto mb-8 bg-red-950/30 border border-red-900 text-red-400 p-4 rounded-lg flex items-start gap-3">
      <AlertCircle className="w-5 h-5 shrink-0 mt-0.5" />
      <div className="text-sm">{error}</div>
    </div>
  );
}

export function SkeletonLoading() {
  return (
    <div className="space-y-8 max-w-5xl mx-auto animate-pulse">
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        {[1,2,3].map(i => <div key={i} className="h-20 bg-zinc-800 rounded-xl" />)}
      </div>
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {[1,2,3,4,5,6].map(i => <div key={i} className="h-64 bg-zinc-800 rounded-xl" />)}
      </div>
    </div>
  );
}

export function EmptyState() {
  return (
    <div className="text-center py-16 bg-zinc-900 border border-zinc-800 rounded-xl max-w-5xl mx-auto">
      <p className="text-zinc-400">No creators found. Try adjusting your search.</p>
    </div>
  );
}
