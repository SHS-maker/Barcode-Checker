import SwiftUI

@main
struct ExpenseTrackerApp: App {
    @StateObject private var store = ExpenseStore()

    var body: some Scene {
        WindowGroup {
            ContentView()
                .environmentObject(store)
        }
    }
}

final class ExpenseStore: ObservableObject {
    @Published var transactions: [ExpenseTransaction]
    @Published var budgets: [Budget]

    init() {
        let mockData = MockBankService.shared
        self.transactions = mockData.transactions
        self.budgets = mockData.budgets
    }

    var totalSpentThisMonth: Double {
        transactions.filter { $0.date.isInCurrentMonth }.reduce(0) { $0 + abs($1.amount) }
    }

    var topCategory: ExpenseCategory {
        let totals = Dictionary(grouping: transactions.filter { $0.date.isInCurrentMonth }, by: { $0.category })
            .mapValues { items in
                items.reduce(0) { result, transaction in
                    result + abs(transaction.amount)
                }
            }

        return totals.max { lhs, rhs in lhs.value < rhs.value }?.key ?? .food
    }

    func addTransaction(_ transaction: ExpenseTransaction) {
        transactions.insert(transaction, at: 0)
    }
}

extension Date {
    var isInCurrentMonth: Bool {
        let calendar = Calendar.current
        return calendar.isDate(self, equalTo: Date(), toGranularity: .month)
    }
}
