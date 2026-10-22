import { Box, ScrollArea } from '@radix-ui/themes';
import { useEffect, useRef } from 'react';

/** Terminal-style output that follows new lines unless the user scrolled up. */
export function LogView({ text, height = 280, placeholder }: { text: string; height?: number; placeholder?: string }) {
  const viewport = useRef<HTMLDivElement>(null);
  const stick = useRef(true);

  useEffect(() => {
    const el = viewport.current;
    if (el && stick.current) el.scrollTop = el.scrollHeight;
  }, [text]);

  return (
    <Box className="log" style={{ height }}>
      <ScrollArea
        ref={viewport}
        type="auto"
        scrollbars="both"
        onScroll={(e) => {
          const el = e.currentTarget;
          stick.current = el.scrollHeight - el.scrollTop - el.clientHeight < 24;
        }}
      >
        <pre className="log-text">{text || <span className="log-placeholder">{placeholder ?? 'No output yet.'}</span>}</pre>
      </ScrollArea>
    </Box>
  );
}
