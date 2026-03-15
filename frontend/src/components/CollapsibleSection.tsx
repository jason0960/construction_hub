import { useState, type ReactNode } from 'react';

interface CollapsibleSectionProps {
  title: string;
  count?: number;
  defaultOpen?: boolean;
  actions?: ReactNode;
  children: ReactNode;
}

export default function CollapsibleSection({
  title,
  count,
  defaultOpen = false,
  actions,
  children,
}: CollapsibleSectionProps) {
  const [open, setOpen] = useState(defaultOpen);

  return (
    <div className="bg-white rounded-xl border border-slate-200 overflow-hidden">
      <button
        onClick={() => setOpen(!open)}
        className="w-full flex items-center justify-between px-4 py-3 hover:bg-slate-50 transition-colors"
      >
        <div className="flex items-center gap-2">
          <ChevronIcon className={`w-4 h-4 text-slate-400 transition-transform ${open ? 'rotate-90' : ''}`} />
          <h3 className="text-sm font-semibold text-slate-900">{title}</h3>
          {count !== undefined && (
            <span className="text-xs bg-slate-100 text-slate-600 px-1.5 py-0.5 rounded-full">
              {count}
            </span>
          )}
        </div>
        {actions && <div onClick={(e) => e.stopPropagation()}>{actions}</div>}
      </button>
      {open && <div className="border-t border-slate-200 px-4 py-3">{children}</div>}
    </div>
  );
}

function ChevronIcon({ className }: { className?: string }) {
  return (
    <svg className={className} fill="none" viewBox="0 0 24 24" strokeWidth={2} stroke="currentColor">
      <path strokeLinecap="round" strokeLinejoin="round" d="M8.25 4.5l7.5 7.5-7.5 7.5" />
    </svg>
  );
}
