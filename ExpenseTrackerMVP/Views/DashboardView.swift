import SwiftUI

struct DashboardView: View {
    @EnvironmentObject private var store: ExpenseStore
    @State private var showBankConnect = false

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 20) {
                    HStack {
                        VStack(alignment: .leading) {
                            Text("Monthly overview")
                                .font(.headline)
                            Text("Family budget")
                                .font(.caption)
                                .foregroundStyle(.secondary)
                        }

                        Spacer()
                        Button {
                            showBankConnect = true
                        } label: {
                            Image(systemName: "plus.circle.fill")
                                .font(.title2)
                        }
                    }

                    MetricRow(store: store)

                    VStack(alignment: .leading, spacing: 12) {
                        Text("Top category")
                            .font(.headline)

                        HStack {
                            Circle()
                                .fill(store.topCategory.color)
                                .frame(width: 16, height: 16)
                            Text(store.topCategory.rawValue)
                                .font(.title3)
                            Spacer()
                            Text(String(format: "€%.2f", (store.transactions.filter { $0.category == store.topCategory && $0.date.isInCurrentMonth }.reduce(0) { $0 + abs($1.amount) })))
                                .fontWeight(.semibold)
                        }
                    }
                    .padding()
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .background(Color(.secondarySystemBackground))
                    .clipShape(RoundedRectangle(cornerRadius: 16))

                    VStack(alignment: .leading, spacing: 12) {
                        Text("Spending by category")
                            .font(.headline)

                        ForEach(ExpenseCategory.allCases, id: \.self) { category in
                            let total = store.transactions.filter { $0.category == category && $0.date.isInCurrentMonth }.reduce(0) { $0 + abs($1.amount) }
                            if total > 0 {
                                VStack(alignment: .leading, spacing: 6) {
                                    HStack {
                                        Text(category.rawValue)
                                        Spacer()
                                        Text(String(format: "€%.2f", total))
                                    }
                                    GeometryReader { proxy in
                                        ZStack(alignment: .leading) {
                                            Capsule().fill(Color.gray.opacity(0.2))
                                            Capsule().fill(category.color)
                                                .frame(width: max(12, min(proxy.size.width, (total / max(store.totalSpentThisMonth, 1)) * proxy.size.width)))
                                        }
                                    }
                                    .frame(height: 10)
                                }
                            }
                        }
                    }
                    .padding()
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .background(Color(.secondarySystemBackground))
                    .clipShape(RoundedRectangle(cornerRadius: 16))
                }
                .padding()
            }
            .navigationTitle("Dashboard")
            .sheet(isPresented: $showBankConnect) {
                BankConnectView()
            }
        }
    }
}

private struct MetricRow: View {
    let store: ExpenseStore

    var body: some View {
        HStack(spacing: 12) {
            MetricCard(title: "Spent", value: String(format: "€%.2f", store.totalSpentThisMonth), color: .red)
            MetricCard(title: "Incoming", value: "€2.400", color: .green)
            MetricCard(title: "Balance", value: "€1.234", color: .blue)
        }
    }
}

private struct MetricCard: View {
    let title: String
    let value: String
    let color: Color

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(title)
                .font(.caption)
                .foregroundStyle(.secondary)
            Text(value)
                .font(.headline)
                .fontWeight(.bold)
        }
        .padding()
        .frame(maxWidth: .infinity, minHeight: 90)
        .background(color.opacity(0.15))
        .clipShape(RoundedRectangle(cornerRadius: 16))
    }
}

#Preview {
    DashboardView()
        .environmentObject(ExpenseStore())
}
