import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { BillingView } from '../BillingView';
import { rest } from 'msw';
import { setupServer } from 'msw/node';

const server = setupServer(
  rest.get('/api/billing/customer/:customerId', (req, res, ctx) => {
    return res(ctx.json([
      {
        id: 'BILL-1',
        policyId: 'POL-1',
        amount: 100.00,
        dueDate: '2025-07-01',
        status: 'PENDING'
      }
    ]));
  })
);

beforeAll(() => server.listen());
afterEach(() => server.resetHandlers());
afterAll(() => server.close());

test('displays customer billing information', async () => {
  render(<BillingView customerId="CUST-1" />);
  
  await waitFor(() => {
    expect(screen.getByText('Policy: POL-1')).toBeInTheDocument();
    expect(screen.getByText('$100.00')).toBeInTheDocument();
    expect(screen.getByText('Due: Jul 1, 2025')).toBeInTheDocument();
  });
});

test('handles payment submission', async () => {
  const user = userEvent.setup();
  render(<BillingView customerId="CUST-1" />);

  await user.click(screen.getByText('Make Payment'));
  await user.type(screen.getByLabelText('Amount'), '100.00');
  await user.click(screen.getByText('Submit Payment'));

  await waitFor(() => {
    expect(screen.getByText('Payment successful')).toBeInTheDocument();
  });
});