import { useEffect, useRef, useState } from "react";
import type { Topic } from "../../types/syllabus";
import { buildLearningPrompt } from "../../lib/learningPrompt";

interface LearnWithAiButtonProps {
  topic: Topic;
  /** "full" = labelled button (topic page); "icon" = compact ✨ (planner rows). */
  variant?: "full" | "icon";
}

/** Copies text to the clipboard, with a legacy fallback for non-secure contexts. */
async function copyText(text: string): Promise<boolean> {
  try {
    if (navigator.clipboard?.writeText) {
      await navigator.clipboard.writeText(text);
      return true;
    }
  } catch {
    /* fall through to the legacy path */
  }
  try {
    const el = document.createElement("textarea");
    el.value = text;
    el.style.position = "fixed";
    el.style.opacity = "0";
    document.body.appendChild(el);
    el.select();
    const ok = document.execCommand("copy");
    document.body.removeChild(el);
    return ok;
  } catch {
    return false;
  }
}

export default function LearnWithAiButton({ topic, variant = "full" }: LearnWithAiButtonProps) {
  const [copied, setCopied] = useState(false);
  const timer = useRef<ReturnType<typeof setTimeout> | null>(null);

  useEffect(() => () => {
    if (timer.current) {
      clearTimeout(timer.current);
    }
  }, []);

  async function handleClick(e: React.MouseEvent) {
    // In the planner rows this button sits next to a Link; don't let the click bubble.
    e.preventDefault();
    e.stopPropagation();
    const ok = await copyText(buildLearningPrompt(topic));
    if (!ok) {
      return;
    }
    setCopied(true);
    if (timer.current) {
      clearTimeout(timer.current);
    }
    timer.current = setTimeout(() => setCopied(false), 2000);
  }

  const title = copied
    ? "Prompt copied — paste it into Claude or ChatGPT"
    : "Copy a ready-made 'teach me this' prompt for Claude or ChatGPT";

  if (variant === "icon") {
    return (
      <button
        type="button"
        onClick={handleClick}
        title={title}
        aria-label={copied ? "Learning prompt copied" : "Copy a Learn with AI prompt"}
        className={`shrink-0 rounded-md border px-1.5 py-0.5 text-xs transition-colors ${
          copied
            ? "border-emerald-300 bg-emerald-50 text-emerald-700"
            : "border-slate-200 text-slate-400 hover:border-slate-300 hover:text-slate-600"
        }`}
      >
        {copied ? "✓" : "✨"}
      </button>
    );
  }

  return (
    <button
      type="button"
      onClick={handleClick}
      title={title}
      className={`inline-flex items-center gap-1.5 rounded-md border px-4 py-2 text-sm font-medium transition-colors ${
        copied
          ? "border-emerald-300 bg-emerald-50 text-emerald-700"
          : "border-indigo-300 bg-indigo-50 text-indigo-700 hover:bg-indigo-100"
      }`}
    >
      {copied ? "✓ Prompt copied — paste into your AI" : "✨ Learn with AI"}
    </button>
  );
}
