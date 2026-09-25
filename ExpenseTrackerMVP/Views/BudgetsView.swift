import SwiftUI

struct BudgetsView: View {
    @EnvironmentObject private var store: ExpenseStore

    var body: some View {
        NavigationStack {
            List {
                ForEach(store.budgets) { budget in
                    VStack(alignment: .leading, spacing: 10) {
                        HStack {
                            Text(budget.category.rawValue)
                                .font(.headline)
                            Spacer()
                            Text(String(format: "€%.0f / €%.0f", budget.spent, budget.limit))
                                .font(.subheadline)
                        }

                        ProgressView(value: budget.progress) {
                            Text("Remaining: €\(String(format: "%.0f", budget.remaining))")
                                .font(.caption)
                                .foregroundStyle(.secondary)
                        }
                        .tint(budget.category.color)
                    }
                    .padding(.vertical, 8)
                }
            }
            .navigationTitle("Budgets")
        }
    }
}

#Preview {
    BudgetsView()
        .environmentObject(ExpenseStore())
}
