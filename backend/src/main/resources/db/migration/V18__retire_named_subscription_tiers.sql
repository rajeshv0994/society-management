UPDATE societies
SET plan_code = 'STANDARD',
    updated_at = now()
WHERE plan_code IN ('BASIC', 'PREMIUM', 'ENTERPRISE');

UPDATE subscription_plans SET name = 'Trial settings' WHERE code = 'TRIAL';
UPDATE subscription_plans SET name = 'Paid usage rates' WHERE code = 'STANDARD';
