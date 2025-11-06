# Kong Wealth Management POC: Step-by-Step Guide

This guide walks you through building a "zero-install" POC using Kong Konnect (for the gateway) and Render (for hosting free backend microservices).

---

## The POC Architecture

- Client (Postman) calls Kong Konnect Gateway with an API Key.  
- Kong validates the API Key and routes the request to the portfolio-service.  
- portfolio-service receives the request, then calls the market-data-service to get live prices.  
- portfolio-service aggregates the data and returns a total portfolio value.  

---

## Phase 1: Deploy Your Backend Microservices (Render)

First, we need to get your two Spring Boot microservices live on the internet. We will use Render's free tier.

### Sign Up

Go to GitHub and Render and create free accounts.

---

### Create a GitHub Repo

1. Create a new, public GitHub repository (e.g., `kong-wealth-poc`).
2. In this new repo, create two folders: `market-data-service` and `portfolio-service`.

You'll need to place the `pom.xml` and the `...Application.java` files (which I provided in the previous step) into their respective folders. Your repo structure should look like this:

```
/market-data-service
pom.xml
src/main/java/com/example/market/MarketApplication.java
/portfolio-service
pom.xml
src/main/java/com/example/portfolio/PortfolioApplication.java
```

Commit and push your code.

---

### Deploy the market-data-service

1. On your Render dashboard, click **New > Web Service**.
2. Connect your GitHub account and select your `kong-wealth-poc` repository.
3. Configure the following:

| Setting | Value |
|----------|--------|
| Name | market-data-service |
| Root Directory | market-data-service (This is important!) |
| Region | Choose a region near you |
| Branch | main (or your default branch) |
| Runtime | Java (Render should detect this from the pom.xml) |
| Instance Type | Free |

Click **Create Web Service**. Wait for it to build and deploy.

Once live, Render will give you a public URL. Copy this URL — it will look like:

```https-market-data-service-xyz.onrender.com```


---

### Deploy the portfolio-service

Follow the exact same steps as above, but with these differences:

| Setting | Value |
|----------|--------|
| Name | portfolio-service |
| Root Directory | portfolio-service |

After it's created, go to the **Environment** tab for your new `portfolio-service`.

Add a new Environment Variable:

| Key | Value |
|-----|--------|
| MARKET_DATA_SERVICE_URL | Paste the URL of your market-data-service (e.g., https-market-data-service-xyz.onrender.com) |

Click **Save Changes** and **Deploy** your service (or trigger a new deploy).

Once live, copy the public URL for your `portfolio-service`. This is the URL you will give to Kong.

---

## Phase 2: Configure the API Gateway (Kong Konnect)

Now we'll use the OpenAPI spec to define your API in Kong.

### Sign Up

Go to [Kong Konnect](https://konnect.konghq.com) and sign up for the Free tier.

---

### Navigate to API Products

In the Konnect dashboard, go to **API Products** from the left-hand menu.

### Create New API Product

1. Click **Add new API product**.  
2. Name: `Wealth Portfolio API`  
3. Version: `v1`  
4. Click **Create**.

---

### Import Your OpenAPI Spec

1. In your new **Wealth Portfolio API** product, go to the `v1` version.  
2. Click **New API Specification**.  
3. Name: `openapi.yaml`  
4. Specification: Click **Upload a file** and upload the `openapi.yaml` file (which I provided in the previous step).

Konnect will parse the spec and automatically create:
- A new **Backend Service** (`portfolio-service`)
- The **Routes** (`/v1/portfolio/{clientId}`)

---

### Configure the Backend Service

1. From the main menu, go to **Gateway Services**.  
2. You will see a service named `portfolio-service` (auto-created from the spec). Click it.  
3. On the `v1` tab, click **Add Backend**.

Enter the following:

| Setting | Value |
|----------|--------|
| Backend URL | Paste your portfolio-service URL from Render (e.g., https-portfolio-service-abc.onrender.com) |

Click **Create** — Kong now knows where to send the traffic.

---

### Enable Security and Create an API Key

The `api-key-auth` plugin was linked by the spec, but we need to **enable** it for this specific API Product.

1. Go back to **API Products > Wealth Portfolio API > v1**.  
2. Find the **Plugins** section and click **Add Plugin**.  
3. Select the **Key Auth** plugin.  
4. Click **Enable**.

Now create a **Consumer** (a user) and give them a key:

1. From the main menu, go to **Consumers**.  
2. Click **New Consumer** → Name: `Postman Tester`.  
3. Go to the new consumer’s **Credentials** tab.  
4. Click **API Key** → **Create API Key**.  
5. Copy the generated key (e.g., `k_...`). You’ll use this in Postman.

---

## Phase 3: Test Your POC (Postman)

### Get Your Kong URL

1. In Konnect, go to **API Products > Wealth Portfolio API > v1**.  
2. Find the route `GET /v1/portfolio/{clientId}`.  
3. Click the **copy** icon next to it to get the full public Konnect URL.

It will look like:

```https-....kong-cloud.com/v1/portfolio/{clientId}```


---

### Test 1: The "Failure" Case (No API Key)

1. Open **Postman**.  
2. Create a new **GET** request.  
3. Paste the Konnect URL (e.g., `https-....kong-cloud.com/v1/portfolio/client-123`).  
4. Click **Send**.  

**Expected Result:**  
```You should get a `401 Unauthorized` error with the message: No API key found in request```


✅ This proves Kong is protecting your endpoint.

---

### Test 2: The "Success" Case (With API Key)

1. In the same Postman request, go to the **Headers** tab.  
2. Add a new header:

| Key | Value |
|-----|--------|
| apikey | Paste the API key you copied from Konnect (e.g., k_...) |

3. Click **Send**.  

**Expected Result:**  
You should get a `200 OK` response with the aggregated JSON from your Spring Boot service:

```json
{
  "clientId": "client-123",
  "totalPortfolioValue": 105650.00,
  "holdings": [
    {
      "symbol": "RNDR",
      "quantity": 500,
      "currentPrice": 12.34,
      "marketValue": 6170.00
    },
    {
      "symbol": "KNG",
      "quantity": 1000,
      "currentPrice": 99.48,
      "marketValue": 99480.00
    }
  ]
}



