@Service
@Slf4j
@RequiredArgsConstructor
public class GracePeriodService {
    private final GracePeriodConfigRepository gracePeriodConfigRepository;
    
    public int getGracePeriodDays(String policyType, PaymentFrequency frequency) {
        return gracePeriodConfigRepository
            .findByPolicyTypeAndFrequency(policyType, frequency)
            .or(() -> gracePeriodConfigRepository.findByPolicyTypeAndFrequency("DEFAULT", frequency))
            .map(GracePeriodConfig::getGracePeriodDays)
            .orElse(10); // Hardcoded fallback
    }
}