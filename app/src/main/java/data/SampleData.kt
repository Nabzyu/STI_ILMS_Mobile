package com.example.stismartlib.data

import com.example.stismartlib.data.models.Book
import com.example.stismartlib.data.models.BorrowStatus
import com.example.stismartlib.data.models.FineRecord
import com.example.stismartlib.data.models.FineStatus
import com.example.stismartlib.data.models.NotificationCategory
import com.example.stismartlib.data.models.NotificationItem
import com.example.stismartlib.data.models.PrintRequest
import com.example.stismartlib.data.models.PrintStatus
import com.example.stismartlib.data.models.Research
import com.example.stismartlib.data.models.Reservation
import com.example.stismartlib.data.models.BorrowingRecord

object SampleData {

    val books = listOf(
        Book(
            id = "b1",
            title = "Introduction to Algorithms",
            author = "Cormen, T. H.",
            isbn = "978-0262046305",
            category = "Computer Science",
            yearPublished = 2022,
            publisher = "MIT Press",
            shelfLocation = "A-101",
            availableCopies = 2,
            totalCopies = 3,
            description = "A comprehensive guide to algorithms and data structures used in modern computing.",
            coverColor = 0xFF1C7ED6
        ),
        Book(
            id = "b2",
            title = "Clean Code",
            author = "Martin, R. C.",
            isbn = "978-0132350884",
            category = "Software Engineering",
            yearPublished = 2008,
            publisher = "Prentice Hall",
            shelfLocation = "A-102",
            availableCopies = 0,
            totalCopies = 2,
            description = "A handbook of agile software craftsmanship with practical coding principles.",
            coverColor = 0xFF364FC7
        ),
        Book(
            id = "b3",
            title = "Database System Concepts",
            author = "Silberschatz, A.",
            isbn = "978-0078022159",
            category = "Information Technology",
            yearPublished = 2020,
            publisher = "McGraw-Hill",
            shelfLocation = "B-201",
            availableCopies = 1,
            totalCopies = 1,
            description = "Fundamentals of database systems including design, SQL, and transaction management.",
            coverColor = 0xFF0CA678
        ),
        Book(
            id = "b4",
            title = "Principles of Marketing",
            author = "Kotler, P.",
            isbn = "978-0134492513",
            category = "Business",
            yearPublished = 2021,
            publisher = "Pearson",
            shelfLocation = "C-301",
            availableCopies = 3,
            totalCopies = 4,
            description = "Core marketing concepts for business students and professionals.",
            coverColor = 0xFFE8590C
        )
    )

    val researchItems = listOf(
        Research(
            id = "r1",
            title = "AI-Based Library Management System",
            author = "Santos, M. A.",
            category = "Information Systems",
            yearPublished = 2024,
            abstract = "This study explores the implementation of artificial intelligence in automating library cataloging and user assistance.",
            shelfLocation = "TH-001",
            coverColor = 0xFF2B8A3E
        ),
        Research(
            id = "r2",
            title = "Impact of Digital Resources on Student Research",
            author = "Reyes, J. P.",
            category = "Education",
            yearPublished = 2023,
            abstract = "An analysis of how digital library resources affect research quality among college students.",
            shelfLocation = "TH-002",
            coverColor = 0xFF5F3DC4
        ),
        Research(
            id = "r3",
            title = "Smart Inventory for Academic Libraries",
            author = "Garcia, L. B.",
            category = "Engineering",
            yearPublished = 2022,
            abstract = "A thesis proposing RFID and barcode integration for real-time book tracking in libraries.",
            shelfLocation = "TH-003",
            coverColor = 0xFF1098AD
        )
    )

    val borrowingHistory = listOf(
        BorrowingRecord(
            id = "br1",
            book = books[0],
            borrowDate = "Aug 1, 2026",
            dueDate = "Aug 2, 2026 8:59 AM",
            returnDate = "Aug 2, 2026",
            borrowStatus = BorrowStatus.RETURNED,
            fineStatus = FineStatus.NONE
        ),
        BorrowingRecord(
            id = "br2",
            book = books[2],
            borrowDate = "Aug 10, 2026",
            dueDate = "Aug 11, 2026 8:59 AM",
            returnDate = null,
            borrowStatus = BorrowStatus.BORROWED,
            fineStatus = FineStatus.NONE
        )
    )

    val fines = listOf(
        FineRecord(
            id = "f1",
            bookTitle = "Web Development Basics",
            amount = 12.0,
            reason = "Overdue — ₱2/hour after 8:59 AM",
            dateIssued = "Jul 15, 2026",
            paymentStatus = FineStatus.PAID,
            paymentDate = "Jul 16, 2026"
        ),
        FineRecord(
            id = "f2",
            bookTitle = "Network Security Essentials",
            amount = 20.0,
            reason = "Overdue — ₱10/day starting next day",
            dateIssued = "Aug 5, 2026",
            paymentStatus = FineStatus.UNPAID,
            paymentDate = null
        )
    )

    val notifications = listOf(
        NotificationItem(
            id = "n1",
            title = "Book Available",
            message = "Clean Code is now available for borrowing.",
            time = "2 hours ago",
            category = NotificationCategory.BOOKS,
            isRead = false
        ),
        NotificationItem(
            id = "n2",
            title = "Due Reminder",
            message = "Database System Concepts is due tomorrow at 8:59 AM.",
            time = "5 hours ago",
            category = NotificationCategory.REMINDERS,
            isRead = false
        ),
        NotificationItem(
            id = "n3",
            title = "Print Ready",
            message = "Your printing request is ready for pickup.",
            time = "1 day ago",
            category = NotificationCategory.PRINTING,
            isRead = true
        ),
        NotificationItem(
            id = "n4",
            title = "Reservation Update",
            message = "You moved to queue position #1 for Clean Code.",
            time = "2 days ago",
            category = NotificationCategory.BOOKS,
            isRead = true
        )
    )

    val reservations = listOf(
        Reservation(
            id = "res1",
            book = books[1],
            queuePosition = 2,
            dateReserved = "Aug 9, 2026"
        )
    )

    val printHistory = listOf(
        PrintRequest(
            id = "p1",
            fileName = "Research_Paper_Final.pdf",
            copies = 2,
            printType = "Document",
            isColored = false,
            note = "Staple on upper left",
            totalCost = 10.0,
            amountPaid = 10.0,
            status = PrintStatus.COMPLETED,
            dateSubmitted = "Aug 8, 2026"
        ),
        PrintRequest(
            id = "p2",
            fileName = "Presentation_Slides.pptx",
            copies = 1,
            printType = "Presentation",
            isColored = true,
            note = "",
            totalCost = 25.0,
            amountPaid = 0.0,
            status = PrintStatus.READY,
            dateSubmitted = "Aug 10, 2026"
        )
    )
}
