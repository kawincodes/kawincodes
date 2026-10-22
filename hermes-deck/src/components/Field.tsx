import { Flex, Text } from '@radix-ui/themes';
import type { ReactNode } from 'react';

export function Field({ label, hint, children }: { label: string; hint?: ReactNode; children: ReactNode }) {
  return (
    <Flex direction="column" gap="1" asChild>
      <label>
        <Text size="2" weight="medium">
          {label}
        </Text>
        {children}
        {hint && (
          <Text size="1" color="gray">
            {hint}
          </Text>
        )}
      </label>
    </Flex>
  );
}
