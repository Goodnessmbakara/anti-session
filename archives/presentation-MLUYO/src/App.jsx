import React, { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { ArrowLeft, ArrowRight, MousePointer2, Brain, Activity, Database, Zap } from 'lucide-react';

const slides = [
  {
    type: 'title',
    tag: 'Vibe Engineering series',
    title: 'Context\nManagement with\nAnti-gravity',
    footer: 'Friday - 27th Feb 2026 | 4:00 PM'
  },
  {
    type: 'content',
    tag: 'Meet the Agent',
    title: 'Who is Anti-gravity?',
    icon: <Zap size={80} className="text-orange-500" />,
    points: [
      'An Agentic Assistant, not a chatbot',
      'Dual-personality: Creative Actor & Critical Critic',
      'Driven by the "Escape Velocity" philosophy',
      'Autonomous research and verification loops'
    ]
  },
  {
    type: 'content',
    tag: 'The Challenge',
    title: 'The Information Entropy',
    icon: <Activity size={80} className="text-orange-500" />,
    points: [
      'The paradox of large context windows: More ≠ Better',
      'High Noise-to-Signal ratio in raw logs',
      'Context Drift: Losing the plot in long sessions',
      'The cognitive load on the Human Collaborative'
    ]
  },
  {
    type: 'content',
    tag: 'Solution Architecture',
    title: 'Knowledge Items (KIs)',
    icon: <Database size={80} className="text-orange-500" />,
    points: [
      'Declarative Memory: The Project Library',
      'Distilled snapshots of architectural truth',
      'Avoids redundant research every turn',
      'Persistent standards for coding and design'
    ]
  },
  {
    type: 'content',
    tag: 'Solution Architecture',
    title: 'Artifacts & Tasks',
    icon: <Brain size={80} className="text-orange-500" />,
    points: [
      'Episodic Memory: The Working State',
      'task.md: The anchoring checklist',
      'implementation_plan.md: The shared blueprint',
      'Checkpoints for seamless backtracking'
    ]
  },
  {
    type: 'content',
    tag: 'The Philosophy',
    title: 'Vibe Engineering',
    icon: <MousePointer2 size={80} className="text-orange-500" />,
    points: [
      'Human-AI Flow: Minimizing technical friction',
      'High-fidelity outputs through context selection',
      'The "Gravity" of tasks managed by the Agent',
      'Total alignment of intent and execution'
    ]
  },
  {
    type: 'title',
    tag: 'Ready to Launch',
    title: 'Reaching\nEscape\nVelocity',
    footer: 'Q&A | Machine Learning UYO'
  }
];

function App() {
  const [currentSlide, setCurrentSlide] = useState(0);

  const next = () => setCurrentSlide((prev) => (prev + 1) % slides.length);
  const prev = () => setCurrentSlide((prev) => (prev - 1 + slides.length) % slides.length);

  useEffect(() => {
    const handleKeyDown = (e) => {
      if (e.key === 'ArrowRight' || e.key === ' ') next();
      if (e.key === 'ArrowLeft') prev();
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, []);

  const slide = slides[currentSlide];

  return (
    <div className="app">
      <div className="progress-bar" style={{ width: `${((currentSlide + 1) / slides.length) * 100}%` }} />
      
      <div className="logo-container">
        <div className="logo-m">M</div>
        <div>
          <div className="logo-text">Machine Learning</div>
          <div className="logo-subtext">UYO</div>
        </div>
      </div>

      <AnimatePresence mode="wait">
        <motion.div
          key={currentSlide}
          initial={{ opacity: 0, x: 20 }}
          animate={{ opacity: 1, x: 0 }}
          exit={{ opacity: 0, x: -20 }}
          transition={{ duration: 0.4, ease: "easeOut" }}
          className="slide-container"
        >
          <div className="text-accent">{slide.tag}</div>
          
          {slide.type === 'title' ? (
            <div className="h-full flex flex-col justify-center">
              <h1 className="heading-xl whitespace-pre-line">{slide.title}</h1>
              {slide.footer && (
                <div className="mt-16 text-3xl font-bold opacity-50">
                   {slide.footer}
                </div>
              )}
            </div>
          ) : (
            <div className="slide-content-grid h-full">
              <div>
                <h2 className="heading-l">{slide.title}</h2>
                <ul className="space-y-8">
                  {slide.points.map((p, i) => (
                    <motion.li 
                      initial={{ opacity: 0, x: -20 }}
                      animate={{ opacity: 1, x: 0 }}
                      transition={{ delay: 0.2 + i * 0.1 }}
                      key={i} 
                      className="text-3xl font-medium tracking-tight leading-snug flex items-start gap-5"
                    >
                      <span className="text-orange-500 mt-2 text-xl">■</span>
                      {p}
                    </motion.li>
                  ))}
                </ul>
              </div>
              <div className="flex justify-center items-center p-10 bg-gray-50 rounded-3xl">
                {slide.icon}
              </div>
            </div>
          )}
        </motion.div>
      </AnimatePresence>

      <div className="controls">
        <button onClick={prev} className="btn-nav"><ArrowLeft size={30} /></button>
        <button onClick={next} className="btn-nav"><ArrowRight size={30} /></button>
      </div>
    </div>
  );
}

export default App;
