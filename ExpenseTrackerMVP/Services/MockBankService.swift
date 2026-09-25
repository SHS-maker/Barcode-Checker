import Foundation

final class MockBankService {
    static let shared = MockBankService()

    let transactions: [ExpenseTransaction]
    let budgets: [Budget]

    private init() {
        let calendar = Calendar.current

        transactions = [
            ExpenseTransaction(
                id: UUID(),
                name: "Groceries",
                category: .food,
                amount: 76.80,
                date: calendar.date(byAdding: .day, value: -1, to: Date()) ?? Date(),
                merchant: "REWE",
                isIncoming: false
            ),
            ExpenseTransaction(
                id: UUID(),
                name: "Rent",
                category: .housing,
                amount: 920.00,
                date: calendar.date(byAdding: .day, value: -4, to: Date()) ?? Date(),
                merchant: "Wohnung",
                isIncoming: false
            ),
            ExpenseTransaction(
                id: UUID(),
                name: "Salary",
                category: .other,
                amount: 2400.00,
                date: calendar.date(byAdding: .day, value: -7, to: Date()) ?? Date(),
                merchant: "Employer",
                isIncoming: true
            ),
            ExpenseTransaction(
                id: UUID(),
                name: "Train ticket",
                category: .transport,
                amount: 42.25,
                date: calendar.date(byAdding: .day, value: -9, to: Date()) ?? Date(),
                merchant: "Deutsche Bahn",
                isIncoming: false
            ),
            ExpenseTransaction(
                id: UUID(),
                name: "Cinema",
                category: .leisure,
                amount: 24.00,
                date: calendar.date(byAdding: .day, value: -10, to: Date()) ?? Date(),
                merchant: "CineStar",
                isIncoming: false
            ),
            ExpenseTransaction(
                id: UUID(),
                name: "Phone bill",
                category: .bills,
                amount: 39.99,
                date: calendar.date(byAdding: .day, value: -13, to: Date()) ?? Date(),
                merchant: "Telekom",
                isIncoming: false
            )
        ]

        budgets = [
            Budget(id: UUID(), category: .food, limit: 420.00, spent: 180.00),
            Budget(id: UUID(), category: .housing, limit: 1200.00, spent: 920.00),
            Budget(id: UUID(), category: .transport, limit: 180.00, spent: 72.00),
            Budget(id: UUID(), category: .leisure, limit: 250.00, spent: 98.00)
        ]
    }
}
