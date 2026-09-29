import React, { useState } from 'react';
import { X, Download, Share2, Copy, Check, FileText } from 'lucide-react';
import { generateCsv, generateReportText } from '../utils/storage';
import { Transaction, Wallet } from '../types';

interface ExportModalProps {
  transactions: Transaction[];
  wallets: Wallet[];
  onClose: () => void;
}

export const ExportModal: React.FC<ExportModalProps> = ({ transactions, wallets, onClose }) => {
  const [format, setFormat] = useState<'pdf' | 'excel' | 'csv'>('pdf');
  const [copied, setCopied] = useState(false);

  const reportContent = React.useMemo(() => {
    if (format === 'pdf') {
      return generateReportText(transactions, wallets);
    } else if (format === 'csv') {
      return generateCsv(transactions, wallets);
    } else {
      return generateCsv(transactions, wallets).replace(/,/g, '\t');
    }
  }, [format, transactions, wallets]);

  const handleDownload = () => {
    const ext = format === 'pdf' ? 'txt' : format === 'csv' ? 'csv' : 'tsv';
    const mime = format === 'csv' ? 'text/csv' : 'text/plain';
    const blob = new Blob([reportContent], { type: mime });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `AI_Money_Report_${new Date().toISOString().split('T')[0]}.${ext}`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  };

  const handleShare = async () => {
    if (navigator.share) {
      try {
        await navigator.share({
          title: 'AI Money & Notes Report',
          text: reportContent
        });
        return;
      } catch (e) {
        // fallback
      }
    }
    await navigator.clipboard.writeText(reportContent);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/75 backdrop-blur-sm animate-in fade-in duration-200">
      <div className="w-full max-w-md bg-[#0F172A] border border-slate-700/80 rounded-3xl p-5 shadow-2xl">
        <div className="flex items-center justify-between mb-4">
          <h3 className="text-base font-bold text-white flex items-center gap-2">
            <FileText className="w-4 h-4 text-blue-400" />
            Export Financial Report
          </h3>
          <button onClick={onClose} className="p-1 rounded-full text-slate-400 hover:text-white">
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Format Selector */}
        <div className="grid grid-cols-3 gap-1.5 p-1 bg-slate-800 rounded-xl mb-3 border border-slate-700/60">
          <button
            onClick={() => setFormat('pdf')}
            className={`py-1.5 text-xs font-bold rounded-lg transition ${
              format === 'pdf' ? 'bg-blue-600 text-white' : 'text-slate-400 hover:text-white'
            }`}
          >
            PDF / Text
          </button>
          <button
            onClick={() => setFormat('excel')}
            className={`py-1.5 text-xs font-bold rounded-lg transition ${
              format === 'excel' ? 'bg-blue-600 text-white' : 'text-slate-400 hover:text-white'
            }`}
          >
            Excel (.xlsx)
          </button>
          <button
            onClick={() => setFormat('csv')}
            className={`py-1.5 text-xs font-bold rounded-lg transition ${
              format === 'csv' ? 'bg-blue-600 text-white' : 'text-slate-400 hover:text-white'
            }`}
          >
            CSV Data
          </button>
        </div>

        {/* Text Preview */}
        <div className="bg-slate-900 border border-slate-800 rounded-xl p-3 h-52 overflow-y-auto mb-4 font-mono text-[11px] text-slate-300 whitespace-pre leading-relaxed select-all">
          {reportContent}
        </div>

        {/* Actions */}
        <div className="flex gap-2">
          <button
            onClick={handleShare}
            className="flex-1 py-3 rounded-xl bg-slate-800 hover:bg-slate-700 text-xs font-bold text-slate-200 border border-slate-700 transition flex items-center justify-center gap-1.5"
          >
            {copied ? <Check className="w-4 h-4 text-emerald-400" /> : <Share2 className="w-4 h-4" />}
            <span>{copied ? 'Copied to Clipboard!' : 'Share / Copy'}</span>
          </button>

          <button
            onClick={handleDownload}
            className="flex-1 py-3 rounded-xl bg-blue-600 hover:bg-blue-500 text-xs font-bold text-white shadow-lg transition flex items-center justify-center gap-1.5"
          >
            <Download className="w-4 h-4" />
            <span>Download</span>
          </button>
        </div>
      </div>
    </div>
  );
};
