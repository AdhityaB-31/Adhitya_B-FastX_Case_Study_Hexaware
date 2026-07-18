export const PASSWORD_RULES = {
    length: { test: (pwd) => pwd.length >= 8, label: "At least 8 characters" },
    uppercase: { test: (pwd) => /[A-Z]/.test(pwd), label: "One uppercase letter" },
    lowercase: { test: (pwd) => /[a-z]/.test(pwd), label: "One lowercase letter" },
    number: { test: (pwd) => /\d/.test(pwd), label: "One number" },
    special: { test: (pwd) => /[@$!%*?&]/.test(pwd), label: "One special character (@$!%*?&)" },
};

export const getPasswordChecks = (password = "") => {
    const checks = {};
    Object.entries(PASSWORD_RULES).forEach(([key, rule]) => {
        checks[key] = rule.test(password);
    });
    return checks;
};

export const isPasswordValid = (password = "") =>
    Object.values(getPasswordChecks(password)).every(Boolean);

const PasswordRequirements = ({ password }) => {
    const checks = getPasswordChecks(password || "");

    return (
        <div style={{ marginTop: "10px", padding: "10px", background: "#f3f4f6", borderRadius: "6px", fontSize: "0.8rem", border: "1px solid #e5e7eb" }}>
            <p style={{ fontWeight: 600, margin: "0 0 6px 0", color: "#4b5563" }}>Password requirements:</p>
            <div style={{ display: "flex", flexDirection: "column", gap: "4px" }}>
                {Object.entries(PASSWORD_RULES).map(([key, rule]) => (
                    <div
                        key={key}
                        style={{
                            display: "flex",
                            alignItems: "center",
                            gap: "6px",
                            color: checks[key] ? "#10b981" : "#ef4444",
                            fontWeight: checks[key] ? 600 : 400,
                        }}
                    >
                        <span>{checks[key] ? "✓" : "✗"}</span> {rule.label}
                    </div>
                ))}
            </div>
        </div>
    );
};

export default PasswordRequirements;
