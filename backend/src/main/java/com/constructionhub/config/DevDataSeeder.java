package com.constructionhub.config;

import com.constructionhub.entity.*;
import com.constructionhub.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DevDataSeeder implements CommandLineRunner {

    private final OrganizationRepository orgRepo;
    private final UserRepository userRepo;
    private final ClientRepository clientRepo;
    private final JobRepository jobRepo;
    private final WorkerRepository workerRepo;
    private final CrewAssignmentRepository crewRepo;
    private final TimeEntryRepository timeRepo;
    private final MaterialRepository materialRepo;
    private final PermitRepository permitRepo;
    private final JobNoteRepository noteRepo;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepo.count() > 0) {
            log.info("Database already seeded, skipping.");
            return;
        }

        log.info("🌱 Seeding dev database...");

        // ── Organization ──
        Organization org = orgRepo.save(Organization.builder()
                .name("Summit Construction LLC")
                .build());

        // ── Owner user (login: jason@summit.com / demo123) ──
        User owner = userRepo.save(User.builder()
                .organization(org)
                .email("jason@summit.com")
                .passwordHash(passwordEncoder.encode("demo123"))
                .role(UserRole.OWNER)
                .firstName("Jason")
                .lastName("Mitchell")
                .phone("(512) 555-0100")
                .active(true)
                .build());

        // ── Clients ──
        Client martinez = clientRepo.save(Client.builder()
                .organization(org)
                .name("David & Maria Martinez")
                .phone("(512) 555-0201")
                .email("martinez.family@email.com")
                .createdBy(owner)
                .build());

        Client techCorp = clientRepo.save(Client.builder()
                .organization(org)
                .name("Hill Country Tech Corp")
                .phone("(512) 555-0302")
                .email("facilities@hctech.com")
                .createdBy(owner)
                .build());

        Client oakdale = clientRepo.save(Client.builder()
                .organization(org)
                .name("Oakdale HOA")
                .phone("(512) 555-0403")
                .email("board@oakdalehoa.com")
                .createdBy(owner)
                .build());

        Client chen = clientRepo.save(Client.builder()
                .organization(org)
                .name("Linda Chen")
                .phone("(512) 555-0504")
                .email("linda.chen@email.com")
                .createdBy(owner)
                .build());

        Client brewCo = clientRepo.save(Client.builder()
                .organization(org)
                .name("Austin Brew Co.")
                .phone("(512) 555-0605")
                .email("ops@austinbrewco.com")
                .createdBy(owner)
                .build());

        // ── Workers ──
        Worker mike = workerRepo.save(Worker.builder()
                .organization(org)
                .firstName("Mike")
                .lastName("Torres")
                .phone("(512) 555-1001")
                .email("mike.torres@email.com")
                .trade("Framing / General")
                .hourlyRate(new BigDecimal("38.00"))
                .status(WorkerStatus.ACTIVE)
                .build());

        Worker sarah = workerRepo.save(Worker.builder()
                .organization(org)
                .firstName("Sarah")
                .lastName("Nguyen")
                .phone("(512) 555-1002")
                .email("sarah.n@email.com")
                .trade("Electrician")
                .hourlyRate(new BigDecimal("52.00"))
                .status(WorkerStatus.ACTIVE)
                .build());

        Worker carlos = workerRepo.save(Worker.builder()
                .organization(org)
                .firstName("Carlos")
                .lastName("Rivera")
                .phone("(512) 555-1003")
                .email("c.rivera@email.com")
                .trade("Plumber")
                .hourlyRate(new BigDecimal("48.00"))
                .status(WorkerStatus.ACTIVE)
                .build());

        Worker james = workerRepo.save(Worker.builder()
                .organization(org)
                .firstName("James")
                .lastName("Whitfield")
                .phone("(512) 555-1004")
                .email("j.whitfield@email.com")
                .trade("HVAC Technician")
                .hourlyRate(new BigDecimal("55.00"))
                .status(WorkerStatus.ACTIVE)
                .build());

        Worker ana = workerRepo.save(Worker.builder()
                .organization(org)
                .firstName("Ana")
                .lastName("Lopez")
                .phone("(512) 555-1005")
                .email("ana.lopez@email.com")
                .trade("Tile / Finish")
                .hourlyRate(new BigDecimal("42.00"))
                .status(WorkerStatus.ACTIVE)
                .build());

        Worker derek = workerRepo.save(Worker.builder()
                .organization(org)
                .firstName("Derek")
                .lastName("Hall")
                .phone("(512) 555-1006")
                .trade("Painter")
                .hourlyRate(new BigDecimal("32.00"))
                .status(WorkerStatus.INACTIVE)
                .build());

        // ═══════════════════════════════════════════
        // JOB 1 — Kitchen Remodel (IN_PROGRESS)
        // ═══════════════════════════════════════════
        Job kitchen = jobRepo.save(Job.builder()
                .organization(org)
                .title("Full Kitchen Remodel")
                .description("Complete tear-out and rebuild of kitchen including new cabinets, quartz counters, tile backsplash, undermount sink, and all electrical/plumbing updates.")
                .client(martinez)
                .status(JobStatus.IN_PROGRESS)
                .siteAddress("4210 Ridgewood Trail")
                .siteCity("Austin")
                .siteState("TX")
                .siteZip("78735")
                .contractPrice(new BigDecimal("67500.00"))
                .startDate(LocalDate.of(2026, 2, 10))
                .estimatedEndDate(LocalDate.of(2026, 4, 15))
                .createdBy(owner)
                .build());

        // Crew
        crewRepo.save(CrewAssignment.builder()
                .job(kitchen).worker(mike).roleOnJob("Lead Framer")
                .startDate(LocalDate.of(2026, 2, 10)).status(AssignmentStatus.ACTIVE).build());
        crewRepo.save(CrewAssignment.builder()
                .job(kitchen).worker(carlos).roleOnJob("Plumbing")
                .startDate(LocalDate.of(2026, 2, 17)).status(AssignmentStatus.ACTIVE).build());
        crewRepo.save(CrewAssignment.builder()
                .job(kitchen).worker(sarah).roleOnJob("Electrical")
                .startDate(LocalDate.of(2026, 2, 17)).status(AssignmentStatus.ASSIGNED).build());
        crewRepo.save(CrewAssignment.builder()
                .job(kitchen).worker(ana).roleOnJob("Tile & Backsplash")
                .startDate(LocalDate.of(2026, 3, 10)).status(AssignmentStatus.ASSIGNED).build());

        // Time entries
        seedTime(kitchen, mike, owner, LocalDate.of(2026, 2, 10), 8, "Demo day — tore out old cabinets and counters");
        seedTime(kitchen, mike, owner, LocalDate.of(2026, 2, 11), 8, "Finished demo, removed old flooring");
        seedTime(kitchen, mike, owner, LocalDate.of(2026, 2, 12), 7, "Framing for new island");
        seedTime(kitchen, mike, owner, LocalDate.of(2026, 2, 13), 8, "Framing continued, header installed");
        seedTime(kitchen, mike, owner, LocalDate.of(2026, 2, 14), 6, "Subfloor repair and leveling");
        seedTime(kitchen, mike, owner, LocalDate.of(2026, 2, 17), 8, "Installed island framing & blocking");
        seedTime(kitchen, mike, owner, LocalDate.of(2026, 2, 18), 8, "Drywall patching");
        seedTime(kitchen, carlos, owner, LocalDate.of(2026, 2, 17), 6, "Rough-in plumbing for sink relocation");
        seedTime(kitchen, carlos, owner, LocalDate.of(2026, 2, 18), 7, "Ran supply lines to island");
        seedTime(kitchen, carlos, owner, LocalDate.of(2026, 2, 19), 5, "Installed drain lines, pressure test");
        seedTime(kitchen, sarah, owner, LocalDate.of(2026, 2, 19), 6, "Ran new 20A circuits for appliances");
        seedTime(kitchen, sarah, owner, LocalDate.of(2026, 2, 20), 7, "Installed recessed light boxes, under-cabinet rough");
        seedTime(kitchen, sarah, owner, LocalDate.of(2026, 2, 21), 4, "Panel update, GFCI circuits");

        // Materials
        seedMaterial(kitchen, "Custom Shaker Cabinets (set)", 1, 12400);
        seedMaterial(kitchen, "Quartz Countertop — Calacatta", 42, 89);  // 42 sqft
        seedMaterial(kitchen, "Porcelain Tile Backsplash", 35, 12);
        seedMaterial(kitchen, "Undermount Sink + Faucet", 1, 680);
        seedMaterial(kitchen, "LED Recessed Lights (6-pack)", 2, 185);
        seedMaterial(kitchen, "Electrical Wire 12/2 (250ft)", 2, 96);
        seedMaterial(kitchen, "PEX Plumbing Supply Kit", 1, 345);
        seedMaterial(kitchen, "Drywall Sheets 4×8", 12, 18);

        // Permits
        permitRepo.save(Permit.builder()
                .job(kitchen).permitType("Building").permitNumber("BLD-2026-04821")
                .issuingAuthority("City of Austin").status(PermitStatus.ACTIVE)
                .fee(new BigDecimal("475.00"))
                .applicationDate(LocalDate.of(2026, 1, 20))
                .issueDate(LocalDate.of(2026, 2, 5))
                .expirationDate(LocalDate.of(2026, 8, 5))
                .build());
        permitRepo.save(Permit.builder()
                .job(kitchen).permitType("Electrical").permitNumber("ELC-2026-01192")
                .issuingAuthority("City of Austin").status(PermitStatus.ACTIVE)
                .fee(new BigDecimal("225.00"))
                .applicationDate(LocalDate.of(2026, 1, 22))
                .issueDate(LocalDate.of(2026, 2, 6))
                .expirationDate(LocalDate.of(2026, 8, 6))
                .build());
        permitRepo.save(Permit.builder()
                .job(kitchen).permitType("Plumbing").permitNumber("PLB-2026-00387")
                .issuingAuthority("City of Austin").status(PermitStatus.ACTIVE)
                .fee(new BigDecimal("200.00"))
                .applicationDate(LocalDate.of(2026, 1, 22))
                .issueDate(LocalDate.of(2026, 2, 7))
                .expirationDate(LocalDate.of(2026, 8, 7))
                .build());

        // Notes
        seedNote(kitchen, owner, "Client wants to upgrade from laminate to quartz — revised contract price to $67,500.", NoteVisibility.OWNER_ONLY);
        seedNote(kitchen, owner, "Plumbing rough-in passed inspection 2/20. Electrical scheduled for 2/24.", NoteVisibility.SHARED);
        seedNote(kitchen, owner, "Mrs. Martinez asked about adding an under-counter wine fridge. Need to quote the electrical add.", NoteVisibility.OWNER_ONLY);

        // ═══════════════════════════════════════════
        // JOB 2 — Office Build-Out (CONTRACTED)
        // ═══════════════════════════════════════════
        Job office = jobRepo.save(Job.builder()
                .organization(org)
                .title("Office Build-Out Suite 200")
                .description("Commercial TI: 3 private offices, open bullpen area, kitchenette, server closet. Includes HVAC mods, electrical, and low-voltage.")
                .client(techCorp)
                .status(JobStatus.CONTRACTED)
                .siteAddress("800 Congress Ave")
                .siteUnit("Suite 200")
                .siteCity("Austin")
                .siteState("TX")
                .siteZip("78701")
                .contractPrice(new BigDecimal("142000.00"))
                .startDate(LocalDate.of(2026, 3, 17))
                .estimatedEndDate(LocalDate.of(2026, 6, 30))
                .createdBy(owner)
                .build());

        crewRepo.save(CrewAssignment.builder()
                .job(office).worker(mike).roleOnJob("Site Lead")
                .startDate(LocalDate.of(2026, 3, 17)).status(AssignmentStatus.ASSIGNED).build());
        crewRepo.save(CrewAssignment.builder()
                .job(office).worker(sarah).roleOnJob("Electrical Lead")
                .startDate(LocalDate.of(2026, 3, 24)).status(AssignmentStatus.ASSIGNED).build());
        crewRepo.save(CrewAssignment.builder()
                .job(office).worker(james).roleOnJob("HVAC Modifications")
                .startDate(LocalDate.of(2026, 4, 1)).status(AssignmentStatus.ASSIGNED).build());

        permitRepo.save(Permit.builder()
                .job(office).permitType("Commercial Building")
                .issuingAuthority("City of Austin").status(PermitStatus.PENDING)
                .fee(new BigDecimal("1200.00"))
                .applicationDate(LocalDate.of(2026, 2, 28))
                .build());

        seedNote(office, owner, "Waiting on commercial building permit — submitted 2/28, expect 2-3 week turnaround.", NoteVisibility.SHARED);
        seedNote(office, owner, "Client wants Cat6A drops at every desk. Budget $4,200 for low-voltage sub.", NoteVisibility.OWNER_ONLY);

        // ═══════════════════════════════════════════
        // JOB 3 — Pool House (ESTIMATED)
        // ═══════════════════════════════════════════
        Job poolhouse = jobRepo.save(Job.builder()
                .organization(org)
                .title("Pool House & Outdoor Kitchen")
                .description("450 sqft pool house with changing room, half bath, and covered outdoor kitchen with built-in grill, smoker, and bar seating.")
                .client(oakdale)
                .status(JobStatus.ESTIMATED)
                .siteAddress("1501 Oakdale Blvd")
                .siteCity("Round Rock")
                .siteState("TX")
                .siteZip("78664")
                .contractPrice(new BigDecimal("95000.00"))
                .startDate(LocalDate.of(2026, 5, 1))
                .estimatedEndDate(LocalDate.of(2026, 8, 15))
                .createdBy(owner)
                .build());

        seedNote(poolhouse, owner, "HOA board meeting March 15 to approve. Expecting signed contract by end of March.", NoteVisibility.OWNER_ONLY);

        // ═══════════════════════════════════════════
        // JOB 4 — Bathroom Remodel (COMPLETED)
        // ═══════════════════════════════════════════
        Job bathroom = jobRepo.save(Job.builder()
                .organization(org)
                .title("Master Bath Renovation")
                .description("Gutted and rebuilt master bath: walk-in shower with frameless glass, freestanding tub, double vanity, heated floors.")
                .client(chen)
                .status(JobStatus.COMPLETED)
                .siteAddress("6722 Barton Hills Dr")
                .siteCity("Austin")
                .siteState("TX")
                .siteZip("78704")
                .contractPrice(new BigDecimal("38500.00"))
                .startDate(LocalDate.of(2025, 11, 4))
                .estimatedEndDate(LocalDate.of(2026, 1, 10))
                .actualEndDate(LocalDate.of(2026, 1, 8))
                .createdBy(owner)
                .build());

        crewRepo.save(CrewAssignment.builder()
                .job(bathroom).worker(carlos).roleOnJob("Lead Plumber")
                .startDate(LocalDate.of(2025, 11, 4)).endDate(LocalDate.of(2026, 1, 8))
                .status(AssignmentStatus.COMPLETED).build());
        crewRepo.save(CrewAssignment.builder()
                .job(bathroom).worker(ana).roleOnJob("Tile & Finish")
                .startDate(LocalDate.of(2025, 11, 18)).endDate(LocalDate.of(2026, 1, 6))
                .status(AssignmentStatus.COMPLETED).build());
        crewRepo.save(CrewAssignment.builder()
                .job(bathroom).worker(sarah).roleOnJob("Electrical / Heated Floor")
                .startDate(LocalDate.of(2025, 11, 11)).endDate(LocalDate.of(2025, 12, 5))
                .status(AssignmentStatus.COMPLETED).build());

        // Time (summarized)
        seedTime(bathroom, carlos, owner, LocalDate.of(2025, 11, 4), 7, "Demo and rough-in plumbing");
        seedTime(bathroom, carlos, owner, LocalDate.of(2025, 11, 5), 8, "Shower pan, drain relocation");
        seedTime(bathroom, carlos, owner, LocalDate.of(2025, 11, 6), 6, "Supply lines, tub hookup");
        seedTime(bathroom, carlos, owner, LocalDate.of(2025, 12, 15), 5, "Final plumbing connections");
        seedTime(bathroom, carlos, owner, LocalDate.of(2026, 1, 5), 4, "Fixture install & test");
        seedTime(bathroom, sarah, owner, LocalDate.of(2025, 11, 11), 6, "Ran circuits for heated floor & exhaust fan");
        seedTime(bathroom, sarah, owner, LocalDate.of(2025, 11, 12), 7, "Heated floor mat wiring, vanity lighting rough-in");
        seedTime(bathroom, sarah, owner, LocalDate.of(2025, 12, 2), 5, "Final electrical — sconces, switches, GFCI");
        seedTime(bathroom, ana, owner, LocalDate.of(2025, 11, 18), 8, "Shower wall waterproofing & prep");
        seedTime(bathroom, ana, owner, LocalDate.of(2025, 11, 19), 8, "Large-format tile install — shower walls");
        seedTime(bathroom, ana, owner, LocalDate.of(2025, 11, 20), 8, "Shower floor mosaic, niche detail");
        seedTime(bathroom, ana, owner, LocalDate.of(2025, 12, 8), 8, "Floor tile — heated floor");
        seedTime(bathroom, ana, owner, LocalDate.of(2025, 12, 9), 7, "Grouting entire bath");
        seedTime(bathroom, ana, owner, LocalDate.of(2026, 1, 3), 5, "Vanity backsplash & caulk");

        seedMaterial(bathroom, "Frameless Glass Shower Encl.", 1, 3200);
        seedMaterial(bathroom, "Freestanding Soaking Tub", 1, 2100);
        seedMaterial(bathroom, "Double Vanity 60\"", 1, 1850);
        seedMaterial(bathroom, "Large Format Wall Tile", 110, 11);
        seedMaterial(bathroom, "Mosaic Floor Tile", 28, 18);
        seedMaterial(bathroom, "Heated Floor Mat Kit", 1, 580);
        seedMaterial(bathroom, "Waterproofing Membrane", 4, 65);

        permitRepo.save(Permit.builder()
                .job(bathroom).permitType("Plumbing").permitNumber("PLB-2025-07721")
                .issuingAuthority("City of Austin").status(PermitStatus.ACTIVE)
                .fee(new BigDecimal("200.00"))
                .applicationDate(LocalDate.of(2025, 10, 20))
                .issueDate(LocalDate.of(2025, 11, 1))
                .expirationDate(LocalDate.of(2026, 5, 1))
                .build());

        seedNote(bathroom, owner, "Client loved the final result. Left us a 5-star review! Great referral potential.", NoteVisibility.SHARED);
        seedNote(bathroom, owner, "Actual costs came in ~$2K under estimate. Profit margin better than projected.", NoteVisibility.OWNER_ONLY);

        // ═══════════════════════════════════════════
        // JOB 5 — Restaurant Remodel (LEAD)
        // ═══════════════════════════════════════════
        Job restaurant = jobRepo.save(Job.builder()
                .organization(org)
                .title("Taproom Expansion & Bar Build")
                .description("Expanding taproom area by 600 sqft, custom bar build with draft system, new seating area, and ADA restroom update.")
                .client(brewCo)
                .status(JobStatus.LEAD)
                .siteAddress("2200 S Lamar Blvd")
                .siteCity("Austin")
                .siteState("TX")
                .siteZip("78704")
                .createdBy(owner)
                .build());

        seedNote(restaurant, owner, "Met with owner March 5. They want to open the expanded space by July 4th weekend. Need to get estimate together ASAP.", NoteVisibility.OWNER_ONLY);

        // ═══════════════════════════════════════════
        // JOB 6 — Fence Project (ON HOLD)
        // ═══════════════════════════════════════════
        Job fence = jobRepo.save(Job.builder()
                .organization(org)
                .title("Cedar Privacy Fence (220 LF)")
                .description("Remove old chain link fence and install 6ft cedar privacy fence with two pedestrian gates and one 12ft double-swing gate.")
                .client(martinez)
                .status(JobStatus.ON_HOLD)
                .siteAddress("4210 Ridgewood Trail")
                .siteCity("Austin")
                .siteState("TX")
                .siteZip("78735")
                .contractPrice(new BigDecimal("12800.00"))
                .startDate(LocalDate.of(2026, 4, 1))
                .estimatedEndDate(LocalDate.of(2026, 4, 10))
                .createdBy(owner)
                .build());

        seedNote(fence, owner, "On hold until kitchen remodel finishes — client doesn't want two crews at the house simultaneously.", NoteVisibility.SHARED);

        log.info("✅ Dev database seeded! Login: jason@summit.com / demo123");
    }

    // ── Helpers ──

    private void seedTime(Job job, Worker worker, User enteredBy, LocalDate date, int hours, String notes) {
        timeRepo.save(TimeEntry.builder()
                .job(job)
                .worker(worker)
                .enteredBy(enteredBy)
                .entryDate(date)
                .hours(new BigDecimal(hours))
                .notes(notes)
                .build());
    }

    private void seedMaterial(Job job, String name, int qty, double unitCost) {
        materialRepo.save(Material.builder()
                .job(job)
                .name(name)
                .quantity(new BigDecimal(qty))
                .unitCost(new BigDecimal(String.valueOf(unitCost)))
                .build());
    }

    private void seedNote(Job job, User author, String content, NoteVisibility visibility) {
        noteRepo.save(JobNote.builder()
                .job(job)
                .author(author)
                .content(content)
                .visibility(visibility)
                .build());
    }
}
