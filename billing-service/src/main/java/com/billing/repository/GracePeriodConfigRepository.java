@Repository
public interface GracePeriodConfigRepository extends JpaRepository<GracePeriodConfig, String> {
    Optional<GracePeriodConfig> findByPolicyTypeAndFrequency(String policyType, PaymentFrequency frequency);
    Optional<GracePeriodConfig> findByPolicyTypeAndFrequencyAndCustomerTier(
        String policyType, 
        PaymentFrequency frequency, 
        CustomerTier customerTier
    );
}