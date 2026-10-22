package com.company.cms.entity;

public enum OrderStatus {
    INQUIRY,
    QUOTED,
    CONFIRMED,
    PAID,
    DELIVERED,
    CANCELLED;

    public boolean canTransitionTo(OrderStatus next) {
        if (this == next) return true;
        if (this == CANCELLED || this == DELIVERED) return false;
        if (next == CANCELLED) return true; // Can cancel before delivery

        return switch (this) {
            case INQUIRY -> next == QUOTED;
            case QUOTED -> next == CONFIRMED;
            case CONFIRMED -> next == PAID;
            case PAID -> next == DELIVERED;
            default -> false;
        };
    }
}
