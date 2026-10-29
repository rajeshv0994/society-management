#!/usr/bin/env python3
"""API-driven SocietyHub cross-role smoke and workflow test.

The default suite is non-destructive apart from short-lived authentication
sessions. Pass --include-mutations only against a disposable/local database.
"""

from __future__ import annotations

import argparse
import concurrent.futures
import datetime as dt
import json
import os
import sys
import threading
import time
import urllib.error
import urllib.request
from pathlib import Path


PORTAL_READS = {
    "super_admin": [
        "/api/auth/me",
        "/api/admin/platform/dashboard",
        "/api/admin/platform/societies",
        "/api/admin/platform/plans",
        "/api/admin/platform/services",
        "/api/admin/platform/orders",
        "/api/admin/platform/payment-gateway",
        "/api/admin/platform/notifications",
        "/api/admin/platform/requirements",
        "/api/admin/platform/custom-plans",
        "/api/admin/platform/storage",
        "/api/admin/platform/support",
        "/api/admin/platform/audit",
    ],
    "society_admin": [
        "/api/auth/me",
        "/api/dashboard/society",
        "/api/society/profile",
        "/api/society/subscription",
        "/api/society/subscription/orders",
        "/api/society/structure",
        "/api/society/residents",
        "/api/society/residents/pending",
        "/api/society/staff",
        "/api/society/configuration",
        "/api/society/documents",
        "/api/society/storage",
        "/api/society/audit",
        "/api/tasks",
        "/api/tasks/options",
        "/api/visitors",
        "/api/visitors/hosts",
        "/api/visitors/targets",
        "/api/complaints",
        "/api/complaints/options",
        "/api/notices",
        "/api/notices/options",
        "/api/amenities",
        "/api/amenities/bookings",
        "/api/billing/invoices",
        "/api/billing/payments",
        "/api/billing/options",
        "/api/billing/finance-summary",
        "/api/billing/charge-settings",
        "/api/billing/expenses",
        "/api/billing/staff-salaries",
        "/api/activities",
        "/api/households",
        "/api/deliveries",
        "/api/emergencies",
        "/api/support",
        "/api/configuration/reference",
    ],
    "staff": [
        "/api/auth/me",
        "/api/dashboard/society",
        "/api/society/staff/me",
        "/api/society/staff/me/attendance",
        "/api/configuration/reference",
        "/api/tasks",
        "/api/tasks/options",
        "/api/visitors",
        "/api/visitors/permissions",
        "/api/visitors/hosts",
        "/api/visitors/targets",
        "/api/complaints",
        "/api/complaints/options",
        "/api/notices",
        "/api/notices/options",
        "/api/amenities",
        "/api/amenities/bookings",
        "/api/billing/invoices",
        "/api/billing/payments",
        "/api/billing/options",
        "/api/activities",
        "/api/households",
        "/api/deliveries",
        "/api/emergencies",
    ],
    "member": [
        "/api/auth/me",
        "/api/dashboard/society",
        "/api/member/profile",
        "/api/configuration/reference",
        "/api/billing/invoices",
        "/api/billing/payments",
        "/api/complaints",
        "/api/complaints/options",
        "/api/visitors",
        "/api/visitors/member-options",
        "/api/notices",
        "/api/notices/options",
        "/api/amenities",
        "/api/amenities/bookings",
        "/api/activities",
        "/api/households",
        "/api/deliveries",
        "/api/emergencies",
    ],
}


class FlowRunner:
    def __init__(self, base_url: str, timeout: float) -> None:
        self.base_url = base_url.rstrip("/")
        self.timeout = timeout
        self.results: list[dict[str, object]] = []
        self.tokens: dict[str, str] = {}
        self.sessions: dict[str, dict[str, object]] = {}
        self._lock = threading.Lock()

    def request(
        self,
        method: str,
        path: str,
        token: str | None = None,
        body: object | None = None,
    ) -> tuple[int, object | None, float]:
        headers = {"Accept": "application/json"}
        data = None
        if token:
            headers["Authorization"] = f"Bearer {token}"
        if body is not None:
            headers["Content-Type"] = "application/json"
            data = json.dumps(body).encode("utf-8")
        request = urllib.request.Request(
            self.base_url + path, data=data, headers=headers, method=method
        )
        started = time.monotonic()
        try:
            with urllib.request.urlopen(request, timeout=self.timeout) as response:
                raw = response.read()
                status = response.status
        except urllib.error.HTTPError as error:
            status = error.code
            raw = error.read()
        elapsed = round((time.monotonic() - started) * 1000, 1)
        if not raw:
            return status, None, elapsed
        try:
            return status, json.loads(raw), elapsed
        except json.JSONDecodeError:
            return status, raw.decode("utf-8", errors="replace"), elapsed

    def expect(
        self,
        case_id: str,
        method: str,
        path: str,
        expected: int | set[int],
        token: str | None = None,
        body: object | None = None,
        validator=None,
    ) -> object | None:
        expected_set = {expected} if isinstance(expected, int) else expected
        try:
            status, payload, elapsed = self.request(method, path, token, body)
            valid = status in expected_set and (validator(payload) if validator else True)
            detail = None if valid else self._detail(payload)
        except Exception as error:  # timeouts and connection failures are test failures
            status, payload, elapsed, valid, detail = 0, None, self.timeout * 1000, False, str(error)
        result = {
            "id": case_id,
            "passed": valid,
            "method": method,
            "path": path,
            "expected": sorted(expected_set),
            "actual": status,
            "elapsed_ms": elapsed,
            "detail": detail,
        }
        with self._lock:
            self.results.append(result)
        return payload if valid else None

    @staticmethod
    def _detail(payload: object | None) -> str:
        if isinstance(payload, dict):
            return str(payload.get("message") or payload.get("error") or payload)[:300]
        return str(payload)[:300]

    def login(self, key: str, email: str, password: str, code: str | None, role: str) -> None:
        payload = self.expect(
            f"AUTH-{key.upper()}-LOGIN",
            "POST",
            "/api/auth/login",
            200,
            body={"email": email, "password": password, "societyCode": code},
            validator=lambda value: isinstance(value, dict) and value.get("role") == role,
        )
        if isinstance(payload, dict):
            self.tokens[key] = str(payload["token"])
            self.sessions[key] = payload

    def run_public_and_auth(self) -> None:
        for index, path in enumerate(
            ["/api/public/plans", "/api/public/services", "/api/public/societies"], start=1
        ):
            self.expect(
                f"PUBLIC-{index:02d}",
                "GET",
                path,
                200,
                validator=lambda value: isinstance(value, list),
            )
        self.expect(
            "AUTH-INVALID-CREDENTIALS",
            "POST",
            "/api/auth/login",
            401,
            body={
                "email": "nobody@example.test",
                "password": "WrongPassword!",
                "societyCode": "GREEN-VALLEY",
            },
        )
        self.login(
            "super_admin",
            os.getenv("SUPER_ADMIN_EMAIL", "admin@society.local"),
            os.getenv("SUPER_ADMIN_PASSWORD", "Admin@12345"),
            None,
            "SUPER_ADMIN",
        )
        self.login(
            "society_admin",
            os.getenv("SOCIETY_ADMIN_EMAIL", "green.admin@society.local"),
            os.getenv("SOCIETY_ADMIN_PASSWORD", "Admin@12345"),
            os.getenv("TEST_SOCIETY_CODE", "GREEN-VALLEY"),
            "SOCIETY_ADMIN",
        )
        self.login(
            "staff",
            os.getenv("STAFF_EMAIL", "staff.green@society.local"),
            os.getenv("STAFF_PASSWORD", "Staff@12345"),
            os.getenv("TEST_SOCIETY_CODE", "GREEN-VALLEY"),
            "STAFF",
        )
        self.login(
            "member",
            os.getenv("MEMBER_EMAIL", "member.green@society.local"),
            os.getenv("MEMBER_PASSWORD", "Member@12345"),
            os.getenv("TEST_SOCIETY_CODE", "GREEN-VALLEY"),
            "RESIDENT",
        )

        # Verify refresh rotation and revocation using a dedicated session.
        session = self.expect(
            "AUTH-ROTATION-LOGIN",
            "POST",
            "/api/auth/login",
            200,
            body={
                "email": os.getenv("MEMBER_EMAIL", "member.green@society.local"),
                "password": os.getenv("MEMBER_PASSWORD", "Member@12345"),
                "societyCode": os.getenv("TEST_SOCIETY_CODE", "GREEN-VALLEY"),
            },
        )
        if isinstance(session, dict):
            old_refresh = session["refreshToken"]
            rotated = self.expect(
                "AUTH-ROTATION-REFRESH",
                "POST",
                "/api/auth/refresh",
                200,
                body={"refreshToken": old_refresh},
            )
            self.expect(
                "AUTH-ROTATION-REUSE-BLOCKED",
                "POST",
                "/api/auth/refresh",
                401,
                body={"refreshToken": old_refresh},
            )
            if isinstance(rotated, dict):
                new_refresh = rotated["refreshToken"]
                self.expect(
                    "AUTH-LOGOUT",
                    "POST",
                    "/api/auth/logout",
                    204,
                    body={"refreshToken": new_refresh},
                )
                self.expect(
                    "AUTH-LOGOUT-REVOKED",
                    "POST",
                    "/api/auth/refresh",
                    401,
                    body={"refreshToken": new_refresh},
                )

    def run_portal_reads(self) -> None:
        jobs = []
        with concurrent.futures.ThreadPoolExecutor(max_workers=12) as executor:
            for role, paths in PORTAL_READS.items():
                token = self.tokens.get(role)
                if not token:
                    continue
                for index, path in enumerate(paths, start=1):
                    jobs.append(
                        executor.submit(
                            self.expect,
                            f"{role.upper()}-LOAD-{index:02d}",
                            "GET",
                            path,
                            200,
                            token,
                            None,
                            lambda value: isinstance(value, (dict, list)),
                        )
                    )
            for job in jobs:
                job.result()

        self.expect(
            "TENANT-SOCIETY-CANNOT-OPEN-PLATFORM",
            "GET",
            "/api/admin/platform/dashboard",
            403,
            self.tokens.get("society_admin"),
        )
        self.expect(
            "ROLE-MEMBER-CANNOT-LIST-RESIDENTS",
            "GET",
            "/api/society/residents",
            403,
            self.tokens.get("member"),
        )
        self.expect(
            "ROLE-STAFF-CANNOT-LIST-ALL-STAFF",
            "GET",
            "/api/society/staff",
            403,
            self.tokens.get("staff"),
        )

    def run_mutations(self) -> None:
        stamp = str(int(time.time() * 1000))
        super_token = self.tokens["super_admin"]
        society_token = self.tokens["society_admin"]
        staff_token = self.tokens["staff"]
        member_token = self.tokens["member"]
        society_id = str(self.sessions["society_admin"]["societyId"])
        staff_id = str(self.sessions["staff"]["userId"])

        self._run_onboarding(stamp)

        ticket = self.expect(
            "FLOW-SUPPORT-CREATE",
            "POST",
            "/api/support",
            200,
            society_token,
            {
                "subject": f"E2E support {stamp}",
                "description": "Automated cross-role support handoff",
                "priority": "NORMAL",
            },
        )
        if isinstance(ticket, dict):
            ticket_id = str(ticket["ticketId"])
            self.expect(
                "FLOW-SUPPORT-SOCIETY-MESSAGE",
                "POST",
                f"/api/support/{ticket_id}/messages",
                200,
                society_token,
                {"message": "Society follow-up"},
            )
            self.expect(
                "FLOW-SUPPORT-SUPER-SEES-TICKET",
                "GET",
                "/api/admin/platform/support",
                200,
                super_token,
                validator=lambda rows: any(str(row.get("id")) == ticket_id for row in rows),
            )
            self.expect(
                "FLOW-SUPPORT-SUPER-REPLY",
                "POST",
                f"/api/support/{ticket_id}/messages",
                200,
                super_token,
                {"message": "Platform response"},
            )
            self.expect(
                "FLOW-SUPPORT-RESOLVE",
                "PATCH",
                f"/api/admin/platform/support/{ticket_id}",
                200,
                super_token,
                {"status": "RESOLVED", "resolution": "Validated by E2E flow"},
            )

        complaint = self.expect(
            "FLOW-COMPLAINT-MEMBER-CREATE",
            "POST",
            "/api/complaints",
            200,
            member_token,
            {
                "title": f"E2E complaint {stamp}",
                "description": "Cross-role complaint lifecycle",
                "priority": "NORMAL",
            },
        )
        if isinstance(complaint, dict):
            complaint_id = str(complaint["id"])
            self.expect(
                "FLOW-COMPLAINT-ASSIGN",
                "PATCH",
                f"/api/complaints/{complaint_id}",
                200,
                society_token,
                {"status": "ASSIGNED", "assignedTo": staff_id, "note": "Assigned by E2E"},
            )
            self.expect(
                "FLOW-COMPLAINT-STAFF-START",
                "PATCH",
                f"/api/complaints/{complaint_id}",
                200,
                staff_token,
                {"status": "IN_PROGRESS", "note": "Work started"},
            )
            self.expect(
                "FLOW-COMPLAINT-MEMBER-COMMENT",
                "POST",
                f"/api/complaints/{complaint_id}/comments",
                200,
                member_token,
                {"note": "Resident confirms access"},
            )
            self.expect(
                "FLOW-COMPLAINT-STAFF-RESOLVE",
                "PATCH",
                f"/api/complaints/{complaint_id}",
                200,
                staff_token,
                {"status": "RESOLVED", "resolution": "Issue resolved in E2E"},
            )

        notice = self.expect(
            "FLOW-NOTICE-PUBLISH",
            "POST",
            "/api/notices",
            200,
            society_token,
            {
                "title": f"E2E notice {stamp}",
                "body": "Automated publication and acknowledgement flow",
                "audience": "ALL",
                "priority": "IMPORTANT",
                "pinned": False,
                "requiresAcknowledgement": True,
                "status": "PUBLISHED",
                "expiresAt": None,
            },
        )
        if isinstance(notice, dict):
            notice_id = str(notice["id"])
            self.expect(
                "FLOW-NOTICE-MEMBER-SEES",
                "GET",
                "/api/notices",
                200,
                member_token,
                validator=lambda rows: any(str(row.get("id")) == notice_id for row in rows),
            )
            self.expect(
                "FLOW-NOTICE-READ",
                "POST",
                f"/api/notices/{notice_id}/read",
                200,
                member_token,
            )
            self.expect(
                "FLOW-NOTICE-ACKNOWLEDGE",
                "POST",
                f"/api/notices/{notice_id}/acknowledge",
                200,
                member_token,
            )

        options = self.expect(
            "FLOW-VISITOR-MEMBER-OPTIONS",
            "GET",
            "/api/visitors/member-options",
            200,
            member_token,
        )
        if isinstance(options, dict) and options.get("spaces"):
            space_id = str(options["spaces"][0]["space_id"])
            visitor = self.expect(
                "FLOW-VISITOR-PRE-REGISTER",
                "POST",
                "/api/visitors",
                200,
                member_token,
                {
                    "spaceId": space_id,
                    "targetType": "RESIDENT",
                    "visitorName": f"E2E Visitor {stamp}",
                    "mobile": "9000000999",
                    "purpose": "Automated flow test",
                    "vehicleNumber": "TEST-01",
                    "expectedAt": (dt.datetime.now(dt.timezone.utc) + dt.timedelta(hours=2)).isoformat(),
                    "accompanyingMembers": 0,
                },
            )
            if isinstance(visitor, dict):
                visitor_id = str(visitor["id"])
                self.expect(
                    "FLOW-VISITOR-CANCEL",
                    "PATCH",
                    f"/api/visitors/{visitor_id}/cancel",
                    200,
                    member_token,
                    {"note": "E2E cleanup"},
                )

        emergency_workspace = self.expect(
            "FLOW-EMERGENCY-OPTIONS",
            "GET",
            "/api/emergencies",
            200,
            member_token,
        )
        if isinstance(emergency_workspace, dict) and emergency_workspace.get("resident_options"):
            flat_id = str(emergency_workspace["resident_options"][0]["flat_id"])
            incident = self.expect(
                "FLOW-EMERGENCY-REPORT",
                "POST",
                "/api/emergencies",
                201,
                member_token,
                {
                    "flatId": flat_id,
                    "incidentType": "OTHER",
                    "severity": "LOW",
                    "title": f"E2E incident {stamp}",
                    "description": "Automated emergency handoff test",
                    "locationDetails": "Test location",
                    "peopleAffected": 0,
                },
            )
            if isinstance(incident, dict):
                incident_id = str(incident["id"])
                self.expect(
                    "FLOW-EMERGENCY-STAFF-ACKNOWLEDGE",
                    "PATCH",
                    f"/api/emergencies/{incident_id}",
                    200,
                    staff_token,
                    {"action": "ACKNOWLEDGE"},
                )
                self.expect(
                    "FLOW-EMERGENCY-STAFF-RESOLVE",
                    "PATCH",
                    f"/api/emergencies/{incident_id}",
                    200,
                    staff_token,
                    {"action": "RESOLVE", "note": "E2E incident resolved"},
                )

        plan = self.expect(
            "FLOW-CUSTOM-PLAN-CREATE-DRAFT",
            "POST",
            "/api/admin/platform/custom-plans",
            200,
            super_token,
            {
                "societyId": society_id,
                "planName": f"E2E draft {stamp}",
                "description": "Disposable cancellation test",
                "enabledModules": ["VISITORS", "BILLING"],
                "maxSpaces": 100,
                "maxStaff": 10,
                "maxStorageGb": 10,
                "pricePerSpace": 10,
                "pricePerStaff": 20,
                "fixedMonthlyPrice": 0,
                "setupFee": 0,
                "billingCycle": "MONTHLY",
                "validFrom": "2099-01-01",
                "validUntil": None,
                "status": "DRAFT",
            },
        )
        if isinstance(plan, dict):
            plan_id = str(plan["customPlanId"])
            self.expect(
                "FLOW-CUSTOM-PLAN-CANCEL",
                "POST",
                f"/api/admin/platform/custom-plans/{plan_id}/cancel",
                200,
                super_token,
                {"reason": "E2E cancellation verification"},
            )
            self.expect(
                "FLOW-CUSTOM-PLAN-CANCEL-TWICE-BLOCKED",
                "POST",
                f"/api/admin/platform/custom-plans/{plan_id}/cancel",
                {400, 409},
                super_token,
                {"reason": "Must not cancel twice"},
            )

        first_invoice = self._issue_and_cancel_invoice(super_token, society_id, "A")
        if first_invoice:
            self._issue_and_cancel_invoice(super_token, society_id, "B")

    def _run_onboarding(self, stamp: str) -> None:
        email = f"e2e.admin.{stamp}@example.test"
        registration = self.expect(
            "FLOW-ONBOARD-REGISTER-TRIAL",
            "POST",
            "/api/public/societies/register",
            200,
            body={
                "name": f"E2E Society {stamp}",
                "societyType": "APARTMENT",
                "totalBlocks": 1,
                "totalSpaces": 2,
                "plannedStaff": 1,
                "approximateResidents": 4,
                "address": "E2E test address",
                "city": "Pune",
                "state": "Maharashtra",
                "pinCode": "411001",
                "contactPerson": "E2E Admin",
                "email": email,
                "mobile": "9000090000",
                "planCode": "TRIAL",
                "enabledModules": ["VISITORS", "BILLING", "COMPLAINTS", "STAFF"],
            },
        )
        if not isinstance(registration, dict) or not registration.get("setupCode"):
            return
        setup_code = str(registration["setupCode"])
        society_id = str(registration["societyId"])
        society_code = str(registration["societyCode"])
        self.expect(
            "FLOW-ONBOARD-ACTIVATE",
            "POST",
            "/api/public/societies/activate",
            200,
            body={
                "setupCode": setup_code,
                "fullName": "E2E Society Admin",
                "email": email,
                "password": "E2eAdmin@123",
                "mobile": "9000090000",
            },
        )
        self.expect(
            "FLOW-ONBOARD-ONE-TIME-CODE",
            "POST",
            "/api/public/societies/activate",
            {400, 409},
            body={
                "setupCode": setup_code,
                "fullName": "Duplicate Admin",
                "email": f"duplicate.{email}",
                "password": "E2eAdmin@123",
                "mobile": "9000090001",
            },
        )
        login = self.expect(
            "FLOW-ONBOARD-ADMIN-LOGIN",
            "POST",
            "/api/auth/login",
            200,
            body={"email": email, "password": "E2eAdmin@123", "societyCode": society_code},
            validator=lambda value: isinstance(value, dict)
            and value.get("role") == "SOCIETY_ADMIN",
        )
        if not isinstance(login, dict):
            return
        token = str(login["token"])
        block = self.expect(
            "FLOW-SETUP-CREATE-BLOCK",
            "POST",
            "/api/society/structure/blocks",
            200,
            token,
            {"name": "Block A"},
        )
        if not isinstance(block, dict):
            return
        section = self.expect(
            "FLOW-SETUP-CREATE-SECTION",
            "POST",
            f"/api/society/structure/blocks/{block['id']}/sections",
            200,
            token,
            {"name": "Ground", "sectionOrder": 0},
        )
        if not isinstance(section, dict):
            return
        space = self.expect(
            "FLOW-SETUP-CREATE-SPACE",
            "POST",
            f"/api/society/structure/sections/{section['id']}/spaces",
            200,
            token,
            {"spaceNumber": "101", "areaSqft": 900},
        )
        if not isinstance(space, dict):
            return

        resident_email = f"e2e.member.{stamp}@example.test"
        resident = self.expect(
            "FLOW-RESIDENT-SELF-REGISTER",
            "POST",
            "/api/public/residents/register",
            200,
            body={
                "societyId": society_id,
                "spaceId": str(space["id"]),
                "residentType": "OWNER",
                "fullName": "E2E Member",
                "mobile": "9000090002",
                "email": resident_email,
                "moveInDate": str(dt.date.today()),
            },
        )
        if isinstance(resident, dict):
            self.expect(
                "FLOW-RESIDENT-APPROVE",
                "POST",
                f"/api/society/residents/{resident['registrationId']}/review",
                200,
                token,
                {"decision": "APPROVE", "temporaryPassword": "E2eMember@123"},
            )
            self.expect(
                "FLOW-RESIDENT-LOGIN",
                "POST",
                "/api/auth/login",
                200,
                body={
                    "email": resident_email,
                    "password": "E2eMember@123",
                    "societyCode": society_code,
                },
                validator=lambda value: isinstance(value, dict) and value.get("role") == "RESIDENT",
            )

        staff_email = f"e2e.staff.{stamp}@example.test"
        staff = self.expect(
            "FLOW-STAFF-CREATE",
            "POST",
            "/api/society/staff",
            200,
            token,
            {
                "fullName": "E2E Staff",
                "email": staff_email,
                "mobile": "9000090003",
                "temporaryPassword": "E2eStaff@123",
                "staffType": "SECURITY",
                "employeeCode": f"E2E-{stamp[-6:]}",
                "dutyArea": "Gate",
                "shiftName": "Day",
            },
        )
        if isinstance(staff, dict):
            self.expect(
                "FLOW-STAFF-PERMISSIONS",
                "PUT",
                f"/api/society/staff/{staff['userId']}/permissions",
                200,
                token,
                {"permissions": ["VISITORS", "COMPLAINTS"]},
            )
            self.expect(
                "FLOW-STAFF-LOGIN",
                "POST",
                "/api/auth/login",
                200,
                body={
                    "email": staff_email,
                    "password": "E2eStaff@123",
                    "societyCode": society_code,
                },
                validator=lambda value: isinstance(value, dict) and value.get("role") == "STAFF",
            )

        self.expect(
            "FLOW-INACTIVE-SOCIETY-SUSPEND",
            "PUT",
            f"/api/admin/platform/societies/{society_id}",
            200,
            self.tokens["super_admin"],
            {
                "name": f"E2E Society {stamp}",
                "description": "Inactive-society authentication regression",
                "website": None,
                "contactPerson": "E2E Admin",
                "email": email,
                "mobile": "9000090000",
                "address": "E2E test address",
                "city": "Pune",
                "state": "Maharashtra",
                "pinCode": "411001",
                "planCode": "TRIAL",
                "plannedStaff": 1,
                "status": "SUSPENDED",
                "enabledModules": ["VISITORS", "BILLING", "COMPLAINTS", "STAFF"],
            },
        )
        self.expect(
            "FLOW-INACTIVE-SOCIETY-LOGIN-MESSAGE",
            "POST",
            "/api/auth/login",
            401,
            body={
                "email": email,
                "password": "E2eAdmin@123",
                "societyCode": society_code,
            },
            validator=lambda value: isinstance(value, dict)
            and "not active" in str(value.get("message", "")).lower(),
        )

    def _issue_and_cancel_invoice(self, token: str, society_id: str, suffix: str) -> bool:
        invoice = self.expect(
            f"FLOW-INVOICE-ISSUE-{suffix}",
            "POST",
            "/api/admin/platform/orders",
            200,
            token,
            {"societyId": society_id, "billingMonth": "2099-01-01", "dueDate": "2099-01-31"},
        )
        if not isinstance(invoice, dict):
            return False
        invoice_id = str(invoice["id"])
        self.expect(
            f"FLOW-INVOICE-CANCEL-{suffix}",
            "POST",
            f"/api/admin/platform/orders/{invoice_id}/cancel",
            200,
            token,
            {"reason": f"E2E cancel and reissue {suffix}"},
            validator=lambda value: isinstance(value, dict) and value.get("status") == "CANCELLED",
        )
        return True

    def finish(self, report_path: Path) -> int:
        ordered = sorted(self.results, key=lambda item: str(item["id"]))
        failures = [result for result in ordered if not result["passed"]]
        report = {
            "generated_at": dt.datetime.now(dt.timezone.utc).isoformat(),
            "base_url": self.base_url,
            "total": len(ordered),
            "passed": len(ordered) - len(failures),
            "failed": len(failures),
            "slowest": sorted(ordered, key=lambda item: float(item["elapsed_ms"]), reverse=True)[:10],
            "results": ordered,
        }
        report_path.parent.mkdir(parents=True, exist_ok=True)
        report_path.write_text(json.dumps(report, indent=2) + "\n", encoding="utf-8")
        print(f"Complete app flow: {report['passed']}/{report['total']} passed")
        print(f"Report: {report_path}")
        if failures:
            for failure in failures:
                print(
                    f"FAIL {failure['id']}: {failure['actual']} for "
                    f"{failure['method']} {failure['path']} ({failure['detail']})"
                )
            return 1
        print(f"Slowest request: {report['slowest'][0]['elapsed_ms']} ms")
        return 0


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser()
    parser.add_argument("--base-url", default=os.getenv("API_BASE_URL", "http://localhost:8080"))
    parser.add_argument("--timeout", type=float, default=10.0)
    parser.add_argument("--include-mutations", action="store_true")
    parser.add_argument(
        "--report",
        type=Path,
        default=Path("build/reports/complete_app_flow_smoke.json"),
    )
    return parser.parse_args()


def main() -> int:
    args = parse_args()
    runner = FlowRunner(args.base_url, args.timeout)
    runner.run_public_and_auth()
    if len(runner.tokens) == 4:
        runner.run_portal_reads()
        if args.include_mutations:
            runner.run_mutations()
    else:
        print("Skipping portal flows because one or more role logins failed")
    return runner.finish(args.report)


if __name__ == "__main__":
    sys.exit(main())
