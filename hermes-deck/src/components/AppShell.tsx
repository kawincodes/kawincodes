import { ArrowLeftIcon } from '@radix-ui/react-icons';
import { Box, Flex, Heading, IconButton, Text } from '@radix-ui/themes';
import type { ReactNode } from 'react';
import { goBack } from '../router';

interface Props {
  title: ReactNode;
  subtitle?: ReactNode;
  back?: boolean;
  actions?: ReactNode;
  footer?: ReactNode;
  children: ReactNode;
}

export function AppShell({ title, subtitle, back, actions, footer, children }: Props) {
  return (
    <Flex direction="column" className="shell">
      <Flex asChild align="center" gap="3" px="4" className="shell-header">
        <header>
          {back && (
            <IconButton variant="ghost" color="gray" size="3" aria-label="Back" onClick={() => goBack()}>
              <ArrowLeftIcon width="20" height="20" />
            </IconButton>
          )}
          <Box flexGrow="1" minWidth="0">
            <Heading size="5" trim="end" truncate>
              {title}
            </Heading>
            {subtitle && (
              <Text as="div" size="1" color="gray" truncate>
                {subtitle}
              </Text>
            )}
          </Box>
          {actions}
        </header>
      </Flex>
      <Box asChild flexGrow="1" px="4" py="4" className="shell-main">
        <main>{children}</main>
      </Box>
      {footer && <Box className="shell-footer">{footer}</Box>}
    </Flex>
  );
}
