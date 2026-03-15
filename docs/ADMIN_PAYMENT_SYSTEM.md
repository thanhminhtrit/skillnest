# Admin/Manager Payment Escrow System - Implementation Guide

## Overview
This document describes the complete implementation of the Admin/Manager role system with 8% platform fee payment escrow for the SkillNest freelancer marketplace.

## Features Implemented

### 1. New Roles
- **ADMIN**: Full system control with financial decisions and user management
- **MANAGER**: Operations, moderation, dispute handling, and payment verification

### 2. Payment Escrow System (8% Platform Fee)

#### Payment Flow
1. **Client Accepts Proposal**
   - Platform calculates: 
     - Platform fee = Total × 8%
     - Student receives = Total × 92%
   - Generates QR code for bank transfer
   - Creates PaymentRequest (status: PENDING_PAYMENT)

2. **Admin/Manager Verifies Payment**
   - Views pending payment requests
   - Verifies bank transfer manually
   - Confirms payment → Auto-creates Contract
   - Updates PaymentRequest status to PAID

3. **Project Completion & Payout**
   - Student marks project completed
   - Client approves (or 7-day auto-approval)
   - Admin releases payment to student (92%)
   - Platform keeps fee (8%)

### 3. Database Schema

#### New Tables

**payment_requests**
- `payment_request_id` (PK)
- `proposal_id` (FK, unique)
- `client_id` (FK)
- `total_amount`, `platform_fee`, `student_amount`
- `status` (PENDING_PAYMENT/PAID/CANCELLED/REFUNDED/RELEASED)
- `qr_code_url`, `payment_reference` (unique)
- `verified_by` (FK to users), `verified_at`
- `created_at`, `updated_at`

**transactions**
- `transaction_id` (PK)
- `contract_id` (FK)
- `from_user_id`, `to_user_id` (FK to users)
- `type` (ESCROW_DEPOSIT/PAYOUT/REFUND/PLATFORM_FEE)
- `amount`, `description`
- `created_at`

### 4. API Endpoints

#### Payment APIs

**POST /api/payments/proposals/{proposalId}/accept**
- Role: CLIENT
- Description: Accept proposal and initiate payment
- Response: QR code, payment reference, bank details

**POST /api/payments/{paymentRequestId}/verify**
- Role: ADMIN/MANAGER
- Description: Verify payment received and create contract

**POST /api/payments/contracts/{contractId}/release**
- Role: ADMIN only
- Description: Release payment to student after completion

**POST /api/payments/contracts/{contractId}/refund**
- Role: ADMIN only
- Description: Refund payment to client (dispute cases)

**GET /api/payments/pending**
- Role: ADMIN/MANAGER
- Description: List all pending payment requests

**GET /api/payments/{paymentRequestId}**
- Role: Authenticated
- Description: Get payment request details

**DELETE /api/payments/{paymentRequestId}**
- Role: CLIENT
- Description: Cancel pending payment request

**GET /api/payments/transactions**
- Role: ADMIN
- Description: View all financial transactions

#### Admin APIs

**GET /api/admin/users**
- Role: ADMIN/MANAGER
- Description: List all users with filters (role, status, search)

**GET /api/admin/users/{userId}**
- Role: ADMIN/MANAGER
- Description: Get user details

**PUT /api/admin/users/{userId}**
- Role: ADMIN/MANAGER
- Description: Update user profile (bio, phone, avatar, etc.)

**PUT /api/admin/users/{userId}/status**
- Role: ADMIN (all statuses) / MANAGER (only ACTIVE ↔ SUSPENDED)
- Description: Change user status
- Statuses: ACTIVE, SUSPENDED, BANNED, DELETED

**DELETE /api/admin/users/{userId}**
- Role: ADMIN only
- Description: Soft delete user (set status to DELETED)

#### Manager APIs (Dispute & Moderation)

**GET /api/manager/disputes**
- Role: ADMIN/MANAGER
- Description: List all disputes with status filter

**PUT /api/manager/disputes/{disputeId}/status**
- Role: ADMIN/MANAGER
- Description: Update dispute status
- Statuses: OPEN → IN_REVIEW → RESOLVED/REJECTED/CLOSED

**GET /api/manager/disputes/{disputeId}/messages**
- Role: ADMIN/MANAGER
- Description: View all messages in disputed contract

**POST /api/manager/messages/system**
- Role: ADMIN/MANAGER
- Description: Post system message to conversation
- Example: "Your dispute is under review"

**PUT /api/manager/messages/{messageId}/hide**
- Role: ADMIN/MANAGER
- Description: Hide message (soft delete for moderation)

**DELETE /api/manager/messages/{messageId}**
- Role: ADMIN only
- Description: Permanently delete message

### 5. Permission Matrix

| Action | ADMIN | MANAGER | CLIENT | STUDENT |
|--------|-------|---------|--------|---------|
| Verify payments | ✅ | ✅ | ❌ | ❌ |
| Release payments | ✅ | ❌ | ❌ | ❌ |
| Refund payments | ✅ | ❌ | ❌ | ❌ |
| View all users | ✅ | ✅ | ❌ | ❌ |
| Update user profiles | ✅ | ✅ | ❌ | ❌ |
| Ban users | ✅ | ❌ | ❌ | ❌ |
| Suspend/Activate users | ✅ | ✅ | ❌ | ❌ |
| Delete users | ✅ | ❌ | ❌ | ❌ |
| View all disputes | ✅ | ✅ | ❌ | ❌ |
| Update dispute status | ✅ | ✅ | ❌ | ❌ |
| View disputed messages | ✅ | ✅ | ❌ | ❌ |
| Post system messages | ✅ | ✅ | ❌ | ❌ |
| Hide messages | ✅ | ✅ | ❌ | ❌ |
| Delete messages permanently | ✅ | ❌ | ❌ | ❌ |

### 6. Security Annotations

Custom annotations for role-based access control:

```java
@IsAdmin // Only ADMIN role
@IsManager // ADMIN or MANAGER roles
```

Usage example:
```java
@PostMapping("/payments/{contractId}/release")
@IsAdmin
public ResponseEntity<ResponseBase> releasePayment(@PathVariable Long contractId) {
    // Only ADMIN can release payments
}
```

### 7. Configuration

**application.yml**
```yaml
payment:
  platform-fee-percent: 8
  bank:
    name: Vietcombank
    account-number: 1234567890
    account-name: SKILLNEST PLATFORM
    code: VCB

azure:
  storage:
    qr-base-url: https://skillneststorage.blob.core.windows.net/qrcodes/
```

### 8. Database Migration

Run the migration script:
```sql
-- File: docs/database/migration_admin_payment_system.sql
-- Creates: payment_requests table, transactions table
-- Adds: ADMIN and MANAGER roles
-- Creates: Indexes for performance
```

Execute migration:
```bash
psql -h localhost -U skillnest -d skillnest_db -f docs/database/migration_admin_payment_system.sql
```

### 9. Business Rules

#### Payment Rules
- Payment must be verified before contract creation
- Only ADMIN can release final payment to student
- Only ADMIN can refund to client
- All money movements tracked in transactions table
- Platform keeps 8% commission on all successful contracts

#### User Management Rules
- MANAGER can only toggle between ACTIVE ↔ SUSPENDED
- MANAGER cannot modify BANNED or DELETED users
- ADMIN has full control over all user statuses
- User deletion is soft delete (status = DELETED)

#### Dispute Rules
- Only contract participants can open disputes
- ADMIN/MANAGER can view all disputes
- ADMIN/MANAGER can update dispute status
- Status flow: OPEN → IN_REVIEW → RESOLVED/REJECTED/CLOSED

#### Message Moderation Rules
- MANAGER can hide messages (soft delete)
- ADMIN can permanently delete messages
- System messages prefixed with "SYSTEM:"
- Hidden messages show "[This message has been hidden by moderator]"

### 10. Testing

#### Create Admin User
```bash
# Password: Admin@123 (hash with BCrypt)
INSERT INTO users (email, password_hash, full_name, status)
VALUES ('admin@skillnest.com', '$2a$10$...', 'System Admin', 'ACTIVE');

INSERT INTO user_roles (user_id, role_id)
SELECT u.user_id, r.role_id
FROM users u, roles r
WHERE u.email = 'admin@skillnest.com' AND r.name = 'ADMIN';
```

#### Test Payment Flow
1. Login as CLIENT → Get token
2. Accept proposal → Receive QR code + payment reference
3. Login as ADMIN/MANAGER → Verify payment
4. Contract auto-created
5. Mark project completed
6. Login as ADMIN → Release payment to student

#### Test Admin Operations
1. Login as ADMIN
2. GET /api/admin/users → View all users
3. PUT /api/admin/users/{id}/status → Suspend user
4. GET /api/manager/disputes → View disputes
5. PUT /api/manager/disputes/{id}/status → Update dispute

### 11. Error Handling

All errors return standardized format:
```json
{
  "statusCode": 400,
  "message": "Validation failed",
  "data": {
    "field": "error message"
  }
}
```

Common errors:
- 400: Validation failed, business rule violation
- 403: Insufficient permissions
- 404: Resource not found
- 409: Conflict (e.g., payment already verified)
- 500: Internal server error

### 12. Monitoring & Logging

All critical operations are logged:
- Payment verification
- Money transfers (release/refund)
- User status changes
- Dispute updates
- Message moderation

Log levels:
- INFO: Successful operations
- WARN: Business rule violations (4xx errors)
- ERROR: System errors (5xx errors)

### 13. Future Enhancements

Potential improvements:
- Automatic payment verification via bank API integration
- Azure Blob Storage integration for QR code images
- Email notifications for payment events
- Webhook support for external systems
- Analytics dashboard for admins
- Automated dispute resolution workflow
- Multi-currency support
- Payment scheduling/installments

## Support

For questions or issues, contact the development team.

---

**Version**: 1.0.0  
**Last Updated**: March 9, 2026  
**Author**: SkillNest Development Team

