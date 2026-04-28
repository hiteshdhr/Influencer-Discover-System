import { Globe, Play, ExternalLink } from 'lucide-react';

export default function CreatorCard({ creator }) {
  return (
    <div className="bg-zinc-900 border border-zinc-800 rounded-xl p-5 flex flex-col h-full hover:border-zinc-700 transition-colors">
      <div className="flex items-start justify-between mb-4">
        <div className="flex items-center gap-3">
          {creator.profileImageUrl ? (
            <img src={creator.profileImageUrl} alt={creator.name} className="w-10 h-10 rounded-full object-cover bg-zinc-800" />
          ) : (
            <div className="w-10 h-10 rounded-full bg-zinc-800 flex items-center justify-center text-sm font-medium text-zinc-300">
              {creator.name?.charAt(0) || '?'}
            </div>
          )}
          <div>
            <h3 className="font-medium text-zinc-100 truncate max-w-[140px]">{creator.name}</h3>
            <div className="flex items-center gap-1.5 text-xs text-zinc-400 mt-0.5">
              {creator.platform?.toLowerCase() === 'instagram' ? <Globe className="w-3 h-3" /> : <Play className="w-3 h-3" />}
              <span>{creator.platform}</span>
            </div>
          </div>
        </div>
        <div className="bg-zinc-950 border border-zinc-800 px-2.5 py-1 rounded-md flex flex-col items-center">
          <span className="text-[10px] text-zinc-500 uppercase font-medium">Fit</span>
          <span className="text-sm font-bold text-zinc-100">{creator.fitScore || 0}</span>
        </div>
      </div>

      <div className="grid grid-cols-3 gap-2 mb-4">
        <div className="bg-zinc-950 border border-zinc-800 rounded-md p-2 text-center">
          <p className="text-[10px] text-zinc-500 uppercase">Followers</p>
          <p className="text-sm font-medium text-zinc-300">{creator.followers > 999 ? (creator.followers/1000).toFixed(1)+'k' : creator.followers || 'N/A'}</p>
        </div>
        <div className="bg-zinc-950 border border-zinc-800 rounded-md p-2 text-center">
          <p className="text-[10px] text-zinc-500 uppercase">Views</p>
          <p className="text-sm font-medium text-zinc-300">{creator.totalViews > 999999 ? (creator.totalViews/1000000).toFixed(1)+'m' : (creator.totalViews/1000).toFixed(1)+'k' || 'N/A'}</p>
        </div>
        <div className="bg-zinc-950 border border-zinc-800 rounded-md p-2 text-center">
          <p className="text-[10px] text-zinc-500 uppercase">Eng. Rate</p>
          <p className="text-sm font-medium text-zinc-300">{creator.engagementRate ? `${creator.engagementRate}%` : 'N/A'}</p>
        </div>
      </div>

      <div className="mb-4 flex-grow">
        <p className="text-xs text-zinc-500 mb-2 font-medium">Themes</p>
        <div className="flex flex-wrap gap-1.5">
          {creator.niche && <span className="bg-blue-900/30 text-blue-400 border border-blue-900/50 px-2 py-0.5 rounded text-xs">{creator.niche}</span>}
          {(creator.recentThemes || []).slice(0, 3).map((t, i) => (
            <span key={i} className="bg-zinc-800 text-zinc-300 px-2 py-0.5 rounded text-xs">{t}</span>
          ))}
        </div>
      </div>

      <a href={creator.channelUrl || '#'} target="_blank" rel="noopener noreferrer" className="flex items-center justify-center gap-2 w-full py-2 bg-zinc-950 hover:bg-zinc-800 border border-zinc-800 rounded-md text-sm font-medium text-zinc-300 transition-colors">
        View Profile <ExternalLink className="w-3.5 h-3.5" />
      </a>
    </div>
  );
}
