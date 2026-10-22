import { EyeClosedIcon, EyeOpenIcon } from '@radix-ui/react-icons';
import { IconButton, TextField } from '@radix-ui/themes';
import { useState } from 'react';

interface Props {
  value: string;
  onChange(value: string): void;
  placeholder?: string;
  autoComplete?: string;
}

export function SecretField({ value, onChange, placeholder, autoComplete = 'off' }: Props) {
  const [shown, setShown] = useState(false);
  return (
    <TextField.Root
      size="3"
      type={shown ? 'text' : 'password'}
      value={value}
      placeholder={placeholder}
      autoComplete={autoComplete}
      autoCapitalize="off"
      autoCorrect="off"
      spellCheck={false}
      onChange={(e) => onChange(e.target.value)}
    >
      <TextField.Slot side="right">
        <IconButton type="button" size="1" variant="ghost" color="gray" aria-label={shown ? 'Hide' : 'Show'} onClick={() => setShown(!shown)}>
          {shown ? <EyeClosedIcon /> : <EyeOpenIcon />}
        </IconButton>
      </TextField.Slot>
    </TextField.Root>
  );
}
