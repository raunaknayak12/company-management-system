import os
import csv
import random
from datetime import datetime, timedelta

DATA_DIR = os.path.join(os.path.dirname(__file__), "..", "..", "analytics", "data")
os.makedirs(DATA_DIR, exist_ok=True)

REGIONS = ["North", "South", "East", "West", "Central"]
CATEGORIES = {
    "Enterprise Software": [
        ("PRD-CRM-01", "Cloud CRM Enterprise", 1200.00, 350.00),
        ("PRD-ERP-02", "Smart ERP Suite", 2500.00, 700.00),
        ("PRD-SEC-03", "CyberShield Pro", 800.00, 200.00)
    ],
    "Cloud Services": [
        ("PRD-CLD-04", "Virtual Cluster 16C", 450.00, 120.00),
        ("PRD-STR-05", "HighIO Storage 10TB", 300.00, 80.00),
        ("PRD-NET-06", "Secure CDN Pipeline", 600.00, 150.00)
    ],
    "Consulting & Support": [
        ("PRD-CON-07", "Architecture Advisory", 1500.00, 400.00),
        ("PRD-SUP-08", "24/7 Dedicated Support", 950.00, 250.00)
    ]
}

EMPLOYEES = [
    ("EMP-001", "Sarah Jenkins", "Sales", "Senior Account Executive"),
    ("EMP-002", "David Chen", "Sales", "Enterprise Solutions Specialist"),
    ("EMP-003", "Elena Rostova", "Sales", "Regional Sales Director"),
    ("EMP-004", "Marcus Vance", "Sales", "Client Success Manager"),
    ("EMP-005", "Amina Patel", "Sales", "Key Account Lead")
]

CUSTOMERS = [
    (101, "Acme Corporation", "North", "Alice Smith", "alice@acme.com"),
    (102, "Nexus Global Tech", "West", "Bob Martin", "bmartin@nexusglobal.com"),
    (103, "Apex Retail Systems", "East", "Clara Oswald", "coswald@apexretail.com"),
    (104, "Horizon Logistics", "South", "Daniel Craig", "dan@horizonlog.com"),
    (105, "BlueSky Finance", "Central", "Evelyn Reed", "evelyn@blueskyfin.com"),
    (106, "Titan Industrial", "North", "Frank Castle", "frank@titanind.com"),
    (107, "Summit Energy Group", "West", "Grace Hopper", "grace@summitenergy.com"),
    (108, "Vanguard Healthcare", "East", "Henry McCoy", "hmccoy@vanguardhealth.com"),
    (109, "Pinnacle Media Group", "South", "Isabella Ross", "iross@pinnacle.com"),
    (110, "Zenith Global Telecom", "Central", "Jack Reacher", "jreacher@zenith.com"),
    (111, "Quantum Dynamics", "North", "Karen Page", "kpage@quantum.io"),
    (112, "OmniCorp International", "West", "Leo Fitz", "lfitz@omnicorp.org"),
    (113, "Solaria Clean Tech", "East", "Mia Wong", "mia@solaria.com"),
    (114, "Atlas Manufacturing", "South", "Nathan Drake", "ndrake@atlasmfg.com"),
    (115, "Nova Financial Partners", "Central", "Olivia Pope", "opope@novafin.com")
]

REVIEW_TEMPLATES = [
    (5, "Exceptional platform performance and top-notch customer support. Streamlined our workflow completely!"),
    (5, "Very satisfied with the speed and reliability. High return on investment within first quarter."),
    (4, "Great software with rich features. Initial onboarding had minor learning curve but smooth afterwards."),
    (4, "Reliable infrastructure and responsive account managers. Would definitely recommend to peers."),
    (3, "Decent overall features, but reporting dashboard could use more customization options."),
    (3, "Meets core expectations, though pricing is somewhat on the higher end for small deployments."),
    (2, "Experienced periodic integration delays during initial cloud migration. Documentation needs improvement."),
    (1, "Frequent timeout errors during peak hours and slow ticket resolution from technical support team."),
    (5, "Unmatched security compliance and outstanding scalability under heavy production load."),
    (4, "Cost-effective licensing and seamless billing process. Highly pleased with the service quality.")
]

def generate_historical_sales(num_records=1200):
    filepath = os.path.join(DATA_DIR, "historical_sales.csv")
    start_date = datetime.now() - timedelta(days=3 * 365)
    
    with open(filepath, "w", newline="", encoding="utf-8") as f:
        writer = csv.writer(f)
        writer.writerow([
            "order_number", "order_date", "customer_id", "customer_name", "region",
            "employee_code", "employee_name", "product_sku", "product_name", "category",
            "quantity", "unit_price", "unit_cost", "discount_pct", "gross_amount", "net_amount",
            "cost_amount", "profit_amount", "status"
        ])
        
        all_products = []
        for cat, prods in CATEGORIES.items():
            for sku, name, price, cost in prods:
                all_products.append((sku, name, cat, price, cost))
        
        statuses = ["DELIVERED", "PAID", "CONFIRMED", "QUOTED", "INQUIRY", "CANCELLED"]
        status_weights = [0.55, 0.20, 0.12, 0.06, 0.04, 0.03]
        
        for i in range(1, num_records + 1):
            order_num = f"ORD-{2023 + (i % 4)}-{10000 + i}"
            days_offset = random.randint(0, 3 * 365)
            order_date = (start_date + timedelta(days=days_offset)).strftime("%Y-%m-%d")
            
            cust = random.choice(CUSTOMERS)
            emp = random.choice(EMPLOYEES)
            prod = random.choice(all_products)
            
            qty = random.randint(1, 15)
            unit_price = prod[3]
            unit_cost = prod[4]
            discount_pct = random.choice([0.0, 0.05, 0.10, 0.15, 0.20])
            
            gross = round(qty * unit_price, 2)
            net = round(gross * (1.0 - discount_pct), 2)
            cost = round(qty * unit_cost, 2)
            profit = round(net - cost, 2)
            
            status = random.choices(statuses, weights=status_weights)[0]
            
            writer.writerow([
                order_num, order_date, cust[0], cust[1], cust[2],
                emp[0], emp[1], prod[0], prod[1], prod[2],
                qty, unit_price, unit_cost, discount_pct * 100, gross, net,
                cost, profit, status
            ])
            
    print(f"Generated {num_records} historical sales rows -> {filepath}")

def generate_reviews(num_records=450):
    filepath = os.path.join(DATA_DIR, "reviews.csv")
    start_date = datetime.now() - timedelta(days=3 * 365)
    
    all_skus = []
    for cat, prods in CATEGORIES.items():
        for sku, _, _, _ in prods:
            all_skus.append(sku)
            
    with open(filepath, "w", newline="", encoding="utf-8") as f:
        writer = csv.writer(f)
        writer.writerow(["review_id", "customer_id", "product_sku", "rating", "review_text", "created_date"])
        
        for i in range(1, num_records + 1):
            rating, text = random.choice(REVIEW_TEMPLATES)
            # Add slight jitter to review text to ensure diversity
            if rating >= 4:
                prefix = random.choice(["Great experience: ", "Highly recommended: ", "Superb outcome: ", ""])
            elif rating == 3:
                prefix = random.choice(["Average service: ", "Neutral feedback: ", "Standard results: ", ""])
            else:
                prefix = random.choice(["Attention needed: ", "Disappointed: ", "Service issue: ", ""])
                
            cust = random.choice(CUSTOMERS)
            sku = random.choice(all_skus)
            days_offset = random.randint(0, 3 * 365)
            review_date = (start_date + timedelta(days=days_offset)).strftime("%Y-%m-%d")
            
            writer.writerow([
                f"REV-{5000 + i}", cust[0], sku, rating, prefix + text, review_date
            ])
            
    print(f"Generated {num_records} reviews rows -> {filepath}")

if __name__ == "__main__":
    generate_historical_sales(1500)
    generate_reviews(500)
