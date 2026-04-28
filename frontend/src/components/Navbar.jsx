import { useState } from 'react';
import { Zap, ChevronDown, User, Settings, LogOut } from 'lucide-react';
import { Link, NavLink } from 'react-router-dom';

export default function Navbar() {
  const [profileOpen, setProfileOpen] = useState(false);

  const navClass = ({ isActive }) => 
    `text-sm font-medium transition-colors ${isActive ? 'text-blue-500' : 'text-zinc-400 hover:text-zinc-100'}`;

  return (
    <header className="border-b border-zinc-800 bg-zinc-950/80 backdrop-blur-md sticky top-0 z-50">
      <div className="mx-auto max-w-5xl px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
        <Link to="/" className="flex items-center gap-2">
          <div className="bg-blue-600 p-1.5 rounded-md">
            <Zap className="w-4 h-4 text-white" />
          </div>
          <span className="font-semibold text-zinc-100 tracking-tight">InfluenceHub AI</span>
        </Link>
        
        <nav className="hidden md:flex items-center gap-6">
          <NavLink to="/" className={navClass} end>Dashboard</NavLink>
          <NavLink to="/discover" className={navClass}>Discover</NavLink>
          <NavLink to="/pricing" className={navClass}>Pricing</NavLink>
        </nav>

        <div className="flex items-center gap-4 relative">
          <button 
            onClick={() => setProfileOpen(!profileOpen)}
            className="w-8 h-8 rounded-full bg-zinc-800 flex items-center justify-center text-xs font-medium text-zinc-300 hover:ring-2 hover:ring-zinc-700 transition-all focus:outline-none"
          >
            U
          </button>
          
          {profileOpen && (
            <div className="absolute top-10 right-0 mt-2 w-48 bg-zinc-900 border border-zinc-800 rounded-lg shadow-xl py-1 z-50">
              <a href="#" className="flex items-center gap-2 px-4 py-2 text-sm text-zinc-300 hover:bg-zinc-800 hover:text-white"><User className="w-4 h-4"/> Profile</a>
              <a href="#" className="flex items-center gap-2 px-4 py-2 text-sm text-zinc-300 hover:bg-zinc-800 hover:text-white"><Settings className="w-4 h-4"/> Settings</a>
              <div className="h-px bg-zinc-800 my-1"></div>
              <a href="#" className="flex items-center gap-2 px-4 py-2 text-sm text-red-400 hover:bg-zinc-800 hover:text-red-300"><LogOut className="w-4 h-4"/> Sign out</a>
            </div>
          )}

          <Link to="/discover" className="hidden sm:inline-flex text-sm font-medium bg-zinc-100 text-zinc-900 px-4 py-2 rounded-md hover:bg-white transition-colors">
            Get Started
          </Link>
        </div>
      </div>
    </header>
  );
}
