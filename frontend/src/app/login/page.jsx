import { Suspense } from 'react';
import LoginClient from '@/components/LoginClient';

export default function LoginPage() {
  return (
    <Suspense fallback={<div>Carregando...</div>}>
      <LoginClient />
    </Suspense>
  );
}
