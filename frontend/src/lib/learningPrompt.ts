import type { Topic } from "../types/syllabus";
import { toTitleCase } from "./topicDisplay";

/**
 * Builds a ready-to-paste "teach me this" prompt for a topic, tailored to the
 * kind of topic it is so the AI coaches the right way:
 *   - practice (DSA): hint-first, attempt-before-answer — never dump the solution
 *   - design (machine coding / LLD): guided design with clarifying questions
 *   - concept (everything else): explain from first principles + active recall
 * Paste it into Claude/ChatGPT to start a focused ~15-minute learning session.
 */
type Track = "concept" | "practice" | "design";

function humanize(slug: string): string {
  return slug.replace(/-/g, " ").trim();
}

/** A readable subject line from the topic's slug id (module, else course). */
function subjectFromId(id: string): string {
  const parts = id.split("/");
  const courseSlug = parts[0].replace(/^geointel-/, "");
  const moduleSlug = parts.length >= 2 ? parts[1] : "";
  return humanize(moduleSlug || courseSlug);
}

function trackFromId(id: string): Track {
  if (id.startsWith("dsa-30/")) {
    return "practice";
  }
  if (id.startsWith("machine-coding-practice/")) {
    return "design";
  }
  return "concept";
}

export function buildLearningPrompt(topic: Topic): string {
  const track = trackFromId(topic.id);
  const level = toTitleCase(topic.difficulty);
  const subject = subjectFromId(topic.id);

  if (track === "practice") {
    return [
      `You are my coding-interview coach. I'm about to solve this problem:`,
      ``,
      `Problem: "${topic.title}"`,
      `Context: DSA interview prep.`,
      ``,
      `Coach me — do NOT give the solution yet:`,
      `1. Restate the problem and its key constraints in one line.`,
      `2. Ask how I'd approach it, then give me ONE small hint only if I'm stuck.`,
      `3. After I've attempted it, review my approach: correctness, time/space complexity, and edge cases.`,
      `4. Then show the optimal solution with a short explanation and the pattern it belongs to.`,
      ``,
      `Start at step 1 and wait for me.`,
    ].join("\n");
  }

  if (track === "design") {
    return [
      `You are my low-level-design mentor. I'm designing this system in Java:`,
      ``,
      `System: "${topic.title}"`,
      `Context: machine-coding / LLD interview practice.`,
      ``,
      `Guide me — don't dump a full solution:`,
      `1. Ask me 2–3 clarifying questions about scope and requirements.`,
      `2. Once I answer, help me identify the core classes, their responsibilities, and relationships.`,
      `3. Discuss the key design patterns and trade-offs that apply.`,
      `4. Point out edge cases and how to keep the design extensible.`,
      ``,
      `Then review the class design I come up with. Start with the clarifying questions.`,
    ].join("\n");
  }

  return [
    `You are my expert tutor. Teach me this topic:`,
    ``,
    `Topic: "${topic.title}"`,
    `Level: ${level}`,
    `Context: part of my studies on ${subject}.`,
    ``,
    `Keep it tight — about a 15-minute read:`,
    `1. Explain the core idea from first principles. Assume I'm a software engineer new to this specific topic.`,
    `2. Give one concrete, real-world example.`,
    `3. Offer a simple analogy.`,
    `4. Call out the 2–3 mistakes or misconceptions people usually have.`,
    ``,
    `Then switch into active-recall mode: ask me 3 questions, one at a time, waiting for my answer and correcting me before the next. End by telling me the one thing I should review next.`,
  ].join("\n");
}
