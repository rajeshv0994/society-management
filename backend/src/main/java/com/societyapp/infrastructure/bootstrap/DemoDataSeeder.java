package com.societyapp.infrastructure.bootstrap;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("local")
@Order(20)
public class DemoDataSeeder implements CommandLineRunner {
    private final JdbcClient db;
    private final PasswordEncoder passwords;
    private final boolean enabled;

    public DemoDataSeeder(
            JdbcClient db,
            PasswordEncoder passwords,
            @Value("${app.seed-demo-data}") boolean enabled) {
        this.db = db;
        this.passwords = passwords;
        this.enabled = enabled;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!enabled) return;
        seed(
                "11111111-1111-1111-1111-111111111111",
                "Green Valley Residency",
                "MH/PUN/2024/1001",
                "Pune",
                "Maharashtra",
                "STANDARD",
                "green.admin@society.local",
                "Green Valley Admin",
                "A",
                6,
                4);
        seed(
                "22222222-2222-2222-2222-222222222222",
                "Sunrise Heights",
                "KA/BLR/2023/2042",
                "Bengaluru",
                "Karnataka",
                "PREMIUM",
                "sunrise.admin@society.local",
                "Sunrise Heights Admin",
                "Tower 1",
                8,
                5);
    }

    private void seed(
            String societyId,
            String name,
            String registration,
            String city,
            String state,
            String plan,
            String email,
            String adminName,
            String blockName,
            int floors,
            int flatsPerFloor) {
        UUID sid = UUID.fromString(societyId);
        db.sql(
                        "insert into societies(id,name,registration_number,society_code,society_type,total_blocks,total_flats,approximate_residents,planned_staff,address,city,state,pin_code,contact_person,email,mobile,plan_code,status,subscription_status,subscription_started_at,subscription_expires_at,approved_at,setup_completed,enabled_modules) values(:id,:n,:r,:code,'COOPERATIVE',1,:flats,:res,2,:address,:city,:state,:pin,:contact,:email,:mobile,:plan,'ACTIVE','ACTIVE',now(),now()+interval '1 year',now(),true,(select jsonb_agg(code order by sort_order,name) from platform_services where status='ACTIVE')) on conflict(registration_number) do nothing")
                .param("id", sid)
                .param("n", name)
                .param("r", registration)
                .param("code", registration.equals("MH/PUN/2024/1001") ? "GREEN-VALLEY" : "SUNRISE")
                .param("flats", floors * flatsPerFloor)
                .param("res", floors * flatsPerFloor * 3)
                .param("address", "Demo address, " + city)
                .param("city", city)
                .param("state", state)
                .param("pin", "411001")
                .param("contact", adminName)
                .param("email", email)
                .param("mobile", "900000000" + (sid.toString().startsWith("1") ? "1" : "2"))
                .param("plan", plan)
                .update();
        UUID actual =
                db.sql("select id from societies where registration_number=:r")
                        .param("r", registration)
                        .query(UUID.class)
                        .single();
        UUID admin =
                db.sql("select id from users where lower(email)=lower(:e)")
                        .param("e", email)
                        .query(UUID.class)
                        .optional()
                        .orElseGet(
                                () ->
                                        db.sql(
                                                        "insert into users(society_id,full_name,email,mobile,password_hash,role,email_verified,mobile_verified) values(:s,:n,:e,'9000000000',:p,'SOCIETY_ADMIN',true,true) returning id")
                                                .param("s", actual)
                                                .param("n", adminName)
                                                .param("e", email)
                                                .param("p", passwords.encode("Admin@12345"))
                                                .query(UUID.class)
                                                .single());
        UUID block =
                db.sql(
                                "insert into blocks(society_id,name) values(:s,:n) on conflict(society_id,name) do update set name=excluded.name returning id")
                        .param("s", actual)
                        .param("n", blockName)
                        .query(UUID.class)
                        .single();
        for (int f = 1; f <= floors; f++) {
            final int floorNo = f;
            UUID floor =
                    db.sql(
                                    "insert into floors(block_id,name,floor_number) values(:b,:n,:f) on conflict(block_id,floor_number) do update set name=excluded.name returning id")
                            .param("b", block)
                            .param("n", "Section " + f)
                            .param("f", f)
                            .query(UUID.class)
                            .single();
            for (int flat = 1; flat <= flatsPerFloor; flat++)
                db.sql(
                                "insert into flats(floor_id,flat_number,area_sqft) values(:f,:n,:a) on conflict(floor_id,flat_number) do nothing")
                        .param("f", floor)
                        .param("n", floorNo + String.format("%02d", flat))
                        .param("a", new BigDecimal(900 + flat * 75))
                        .update();
        }
        if (name.startsWith("Green")) {
            seedTestStaff(actual);
            seedTestMember(actual);
        }
        if (db.sql("select count(*) from notices where society_id=:s")
                        .param("s", actual)
                        .query(Long.class)
                        .single()
                == 0)
            db.sql(
                            "insert into notices(society_id,title,body,audience,published_by,updated_at) values(:s,'Welcome to SocietyHub','This is seeded demonstration data for portal testing.','ALL',:u,now())")
                    .param("s", actual)
                    .param("u", admin)
                    .update();
        if (db.sql("select count(*) from support_tickets where society_id=:s")
                        .param("s", actual)
                        .query(Long.class)
                        .single()
                == 0)
            db.sql(
                            "insert into support_tickets(society_id,created_by,subject,description,priority) values(:s,:u,'Demo onboarding assistance','Please verify the demonstration support workflow.','NORMAL')")
                    .param("s", actual)
                    .param("u", admin)
                    .update();
        db.sql(
                        "insert into support_ticket_messages(ticket_id,sender_id,message) select t.id,:u,t.description from support_tickets t where t.society_id=:s and not exists(select 1 from support_ticket_messages m where m.ticket_id=t.id)")
                .param("u", admin)
                .param("s", actual)
                .update();
        if (db.sql("select count(*) from subscription_orders where society_id=:s")
                        .param("s", actual)
                        .query(Long.class)
                        .single()
                == 0)
            db.sql(
                            "insert into subscription_orders(society_id,invoice_number,billing_month,due_date,billing_cycle,period_start,period_end,plan_code,amount,provider,provider_reference,payment_method,status,submitted_at,reviewed_at,paid_at) select :s,:invoice,date_trunc('month',now())::date,current_date,'MONTHLY',date_trunc('month',now())::date,(date_trunc('month',now())+interval '1 month - 1 day')::date,:p,price_per_space*(select total_flats from societies where id=:s)+price_per_staff*(select planned_staff from societies where id=:s),'MOCK',:ref,'MOCK','PAID',now(),now(),now() from subscription_plans where code=:p")
                    .param("s", actual)
                    .param("p", plan)
                    .param("invoice", "SUB-DEMO-" + registration)
                    .param("ref", "DEMO-" + registration)
                    .update();
        if (db.sql("select count(*) from notification_outbox where society_id=:s")
                        .param("s", actual)
                        .query(Long.class)
                        .single()
                == 0)
            db.sql(
                            "insert into notification_outbox(society_id,channel,destination,template_code,payload,status,sent_at) values(:s,'EMAIL',:e,'SOCIETY_WELCOME','{}','SENT',now())")
                    .param("s", actual)
                    .param("e", email)
                    .update();
        if (db.sql("select count(*) from audit_events where society_id=:s")
                        .param("s", actual)
                        .query(Long.class)
                        .single()
                == 0)
            db.sql(
                            "insert into audit_events(society_id,actor_user_id,action,entity_type,entity_id,details) values(:s,:u,'DEMO_SOCIETY_SEEDED','SOCIETY',cast(:s as varchar),'{}')")
                    .param("s", actual)
                    .param("u", admin)
                    .update();
        if (db.sql("select count(*) from requirement_requests where society_id=:s")
                                .param("s", actual)
                                .query(Long.class)
                                .single()
                        == 0
                && name.startsWith("Green"))
            db.sql(
                            "insert into requirement_requests(society_id,society_name,contact_person,email,mobile,city,estimated_flats,requested_modules,requirement_details,budget_monthly) values(:s,:n,:c,:e,'9000000001',:city,:flats,'[\"VISITORS\",\"BILLING\",\"CUSTOM_REPORTS\"]','Need custom accounting exports and additional visitor security workflow.',8500)")
                    .param("s", actual)
                    .param("n", name)
                    .param("c", adminName)
                    .param("e", email)
                    .param("city", city)
                    .param("flats", floors * flatsPerFloor)
                    .update();
    }

    private void seedTestStaff(UUID societyId) {
        UUID userId =
                findOrCreateTestUser(
                        societyId,
                        "Demo Security Staff",
                        "staff.green@society.local",
                        "9000000011",
                        "Staff@12345",
                        "STAFF");
        db.sql(
                        "insert into staff_profiles(user_id,staff_type,employee_code,duty_area,shift_name) values(:u,'SECURITY','DEMO-SEC-001','Main Gate','Morning') on conflict(user_id) do nothing")
                .param("u", userId)
                .update();
        for (String permission :
                List.of(
                        "TASKS",
                        "VISITORS",
                        "COMPLAINTS",
                        "PAYMENTS",
                        "AMENITIES",
                        "ACTIVITIES_GALLERIES",
                        "HOUSEHOLDS",
                        "NOTICES",
                        "DELIVERIES",
                        "EMERGENCY_INCIDENTS")) {
            db.sql(
                            "insert into staff_permissions(staff_user_id,permission_code,granted_by) select :u,:permission,id from users where society_id=:s and role='SOCIETY_ADMIN' order by created_at limit 1 on conflict(staff_user_id,permission_code) do nothing")
                    .param("u", userId)
                    .param("permission", permission)
                    .param("s", societyId)
                    .update();
        }
    }

    private void seedTestMember(UUID societyId) {
        UUID userId =
                findOrCreateTestUser(
                        societyId,
                        "Demo Society Member",
                        "member.green@society.local",
                        "9000000021",
                        "Member@12345",
                        "RESIDENT");
        db.sql(
                        "update flats set active_owner_id=:u, occupancy_status='OWNER_OCCUPIED' where id=(select f.id from flats f join floors fl on fl.id=f.floor_id join blocks b on b.id=fl.block_id where b.society_id=:s and (f.active_owner_id is null or f.active_owner_id=:u) order by fl.floor_number,f.flat_number limit 1)")
                .param("u", userId)
                .param("s", societyId)
                .update();
    }

    private UUID findOrCreateTestUser(
            UUID societyId,
            String fullName,
            String email,
            String mobile,
            String password,
            String role) {
        return db.sql("select id from users where lower(email)=lower(:e)")
                .param("e", email)
                .query(UUID.class)
                .optional()
                .orElseGet(
                        () ->
                                db.sql(
                                                "insert into users(society_id,full_name,email,mobile,password_hash,role,email_verified,mobile_verified) values(:s,:n,:e,:m,:p,:r,true,true) returning id")
                                        .param("s", societyId)
                                        .param("n", fullName)
                                        .param("e", email)
                                        .param("m", mobile)
                                        .param("p", passwords.encode(password))
                                        .param("r", role)
                                        .query(UUID.class)
                                        .single());
    }
}
