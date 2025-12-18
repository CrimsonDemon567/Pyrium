# Minimal DSL:
# - log("message")
# - mul_speed("MobType", factor)

def on_tick():
    log("speeding up zombies")
    mul_speed("Zombie", 1.05)
