@Entity
@Table(name = "grace_period_configs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GracePeriodConfig {
    @Id
    private String id;
    
    @Column(name = "policy_type")
    private String policyType;
    
    @Column(name = "payment_frequency")
    @Enumerated(EnumType.STRING)
    private PaymentFrequency frequency;
    
    @Column(name = "grace_period_days")
    private Integer gracePeriodDays;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}