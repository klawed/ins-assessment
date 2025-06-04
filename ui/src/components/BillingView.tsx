import React from 'react';
import { useMutation, useQuery } from '@tanstack/react-query';
import { billingApi } from '../api/billingApi';

export const BillingView: React.FC<{ customerId: string }> = ({ customerId }) => {
  const { data: billings } = useQuery(['billings', customerId], () => 
    billingApi.getCustomerBillings(customerId)
  );

  const paymentMutation = useMutation(billingApi.submitPayment);

  return (
    <div>
      <h2>Billing History</h2>
      {billings?.map(billing => (
        <div key={billing.id}>
          <p>Policy: {billing.policyId}</p>
          <p>${billing.amount}</p>
          <p>Due: {new Date(billing.dueDate).toLocaleDateString()}</p>
          <button onClick={() => paymentMutation.mutate({
            billingId: billing.id,
            amount: billing.amount
          })}>
            Make Payment
          </button>
        </div>
      ))}
    </div>
  );
};