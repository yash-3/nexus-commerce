#!/usr/bin/env bash
# ==============================================================================
# Nexus Commerce - CI/CD, Kubernetes Manifests & Helm Charts Installer
# Run this script directly inside your local 'nexus-commerce' git repository:
#   cd /path/to/nexus-commerce
#   bash setup-cicd-k8s.sh
# ==============================================================================

set -euo pipefail

echo "==> Setting up CI/CD, Dockerfiles, Kubernetes manifests, and Helm charts in $(pwd)..."

mkdir -p "eureka-server"
cat <<'EOF_FILE_0' > "eureka-server/Dockerfile"
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Non-root user for container security
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

EXPOSE 8761

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
EOF_FILE_0
echo "  [+] Created eureka-server/Dockerfile"

mkdir -p "api-gateway"
cat <<'EOF_FILE_1' > "api-gateway/Dockerfile"
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Non-root user for container security
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

EXPOSE 8080

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
EOF_FILE_1
echo "  [+] Created api-gateway/Dockerfile"

mkdir -p "product-service"
cat <<'EOF_FILE_2' > "product-service/Dockerfile"
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Non-root user for container security
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

EXPOSE 8081

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
EOF_FILE_2
echo "  [+] Created product-service/Dockerfile"

mkdir -p "inventory-service"
cat <<'EOF_FILE_3' > "inventory-service/Dockerfile"
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Non-root user for container security
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

EXPOSE 8082

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
EOF_FILE_3
echo "  [+] Created inventory-service/Dockerfile"

mkdir -p "order-service"
cat <<'EOF_FILE_4' > "order-service/Dockerfile"
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Non-root user for container security
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

EXPOSE 8083

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
EOF_FILE_4
echo "  [+] Created order-service/Dockerfile"

mkdir -p "payment-service"
cat <<'EOF_FILE_5' > "payment-service/Dockerfile"
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Non-root user for container security
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

EXPOSE 8084

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
EOF_FILE_5
echo "  [+] Created payment-service/Dockerfile"

mkdir -p "notification-service"
cat <<'EOF_FILE_6' > "notification-service/Dockerfile"
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Non-root user for container security
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

EXPOSE 8085

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
EOF_FILE_6
echo "  [+] Created notification-service/Dockerfile"

mkdir -p "ai-compliance-service"
cat <<'EOF_FILE_7' > "ai-compliance-service/Dockerfile"
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Non-root user for container security
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

EXPOSE 8086

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
EOF_FILE_7
echo "  [+] Created ai-compliance-service/Dockerfile"

mkdir -p ".github/workflows"
cat <<'EOF_FILE_8' > ".github/workflows/ci-cd.yml"
name: Nexus Commerce CI/CD Pipeline

on:
  push:
    branches: [ "main" ]
  pull_request:
    branches: [ "main" ]
  workflow_dispatch:

env:
  REGISTRY: ghcr.io
  IMAGE_PREFIX: ${{ github.repository_owner }}/nexus-commerce

jobs:
  # ---------------------------------------------------------------------------
  # 1. Maven Compile, Unit Test & Build Artifacts
  # ---------------------------------------------------------------------------
  build-and-test:
    name: Build & Test (Java 21)
    runs-on: ubuntu-latest
    steps:
      - name: Checkout Source Code
        uses: actions/checkout@v4

      - name: Set up JDK 21 (Eclipse Temurin)
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
          cache: 'maven'

      - name: Run Maven Tests
        run: mvn clean test -B

      - name: Package Service JARs
        run: mvn package -DskipTests -B

      - name: Archive Build JARs
        uses: actions/upload-artifact@v4
        with:
          name: service-jars
          path: |
            **/target/*.jar
            !**/target/*-sources.jar
          retention-days: 1

  # ---------------------------------------------------------------------------
  # 2. Build & Push Multi-Microservice Docker Images (Matrix)
  # ---------------------------------------------------------------------------
  docker-build-push:
    name: Docker Build & Push (${{ matrix.service }})
    needs: build-and-test
    runs-on: ubuntu-latest
    permissions:
      contents: read
      packages: write
    strategy:
      fail-fast: false
      matrix:
        service:
          - eureka-server
          - api-gateway
          - product-service
          - inventory-service
          - order-service
          - payment-service
          - notification-service
          - ai-compliance-service
    steps:
      - name: Checkout Repository
        uses: actions/checkout@v4

      - name: Download Packaged JARs
        uses: actions/download-artifact@v4
        with:
          name: service-jars

      - name: Set up Docker Buildx
        uses: docker/setup-buildx-action@v3

      - name: Log in to GitHub Container Registry (GHCR)
        if: github.event_name != 'pull_request'
        uses: docker/login-action@v3
        with:
          registry: ${{ env.REGISTRY }}
          username: ${{ github.actor }}
          password: ${{ secrets.GITHUB_TOKEN }}

      - name: Extract Docker Metadata
        id: meta
        uses: docker/metadata-action@v5
        with:
          images: ${{ env.REGISTRY }}/${{ env.IMAGE_PREFIX }}/${{ matrix.service }}
          tags: |
            type=sha,format=short,prefix=
            type=raw,value=latest,enable={{is_default_branch}}

      - name: Build and Push Docker Image
        uses: docker/build-push-action@v5
        with:
          context: ./${{ matrix.service }}
          file: ./${{ matrix.service }}/Dockerfile
          push: ${{ github.event_name != 'pull_request' }}
          tags: ${{ steps.meta.outputs.tags }}
          labels: ${{ steps.meta.outputs.labels }}
          cache-from: type=gha
          cache-to: type=gha,mode=max

  # ---------------------------------------------------------------------------
  # 3. Kubernetes Manifest Validation & Continuous Deployment
  # ---------------------------------------------------------------------------
  k8s-validate-and-deploy:
    name: Validate & Deploy Manifests
    needs: docker-build-push
    runs-on: ubuntu-latest
    steps:
      - name: Checkout Repository
        uses: actions/checkout@v4

      - name: Set up Kubectl CLI
        uses: azure/setup-kubectl@v4
        with:
          version: 'latest'

      - name: Client-Side Dry-Run Validation
        run: |
          echo "Validating Kubernetes manifests..."
          kubectl apply --dry-run=client -f k8s/

      - name: Deploy to Kubernetes Cluster (Optional / Configured Secret)
        if: github.ref == 'refs/heads/main' && env.KUBECONFIG_DATA != ''
        env:
          KUBECONFIG_DATA: ${{ secrets.KUBECONFIG }}
        run: |
          mkdir -p ~/.kube
          echo "${KUBECONFIG_DATA}" | base64 -d > ~/.kube/config
          kubectl apply -f k8s/
          kubectl rollout status deployment/api-gateway -n nexus-commerce --timeout=120s
EOF_FILE_8
echo "  [+] Created .github/workflows/ci-cd.yml"

mkdir -p "k8s"
cat <<'EOF_FILE_9' > "k8s/00-namespace.yaml"
apiVersion: v1
kind: Namespace
metadata:
  name: nexus-commerce
  labels:
    app.kubernetes.io/name: nexus-commerce
EOF_FILE_9
echo "  [+] Created k8s/00-namespace.yaml"

mkdir -p "k8s"
cat <<'EOF_FILE_10' > "k8s/01-configmap-secrets.yaml"
apiVersion: v1
kind: ConfigMap
metadata:
  name: nexus-global-config
  namespace: nexus-commerce
data:
  EUREKA_SERVER_URL: "http://eureka-server.nexus-commerce.svc.cluster.local:8761/eureka/"
  KAFKA_BOOTSTRAP_SERVERS: "kafka-service.nexus-commerce.svc.cluster.local:9092"
  POSTGRES_HOST: "postgres-service.nexus-commerce.svc.cluster.local"
  POSTGRES_PORT: "5432"
  SPRING_PROFILES_ACTIVE: "prod"
---
apiVersion: v1
kind: Secret
metadata:
  name: nexus-db-secrets
  namespace: nexus-commerce
type: Opaque
stringData:
  POSTGRES_USER: "postgres"
  POSTGRES_PASSWORD: "postgres_secure_password_2026"
EOF_FILE_10
echo "  [+] Created k8s/01-configmap-secrets.yaml"

mkdir -p "k8s"
cat <<'EOF_FILE_11' > "k8s/02-postgres.yaml"
apiVersion: v1
kind: ConfigMap
metadata:
  name: postgres-init-script
  namespace: nexus-commerce
data:
  init-db.sql: |
    CREATE DATABASE product_db;
    CREATE DATABASE inventory_db;
    CREATE DATABASE order_db;
    CREATE DATABASE payment_db;
    GRANT ALL PRIVILEGES ON DATABASE product_db TO postgres;
    GRANT ALL PRIVILEGES ON DATABASE inventory_db TO postgres;
    GRANT ALL PRIVILEGES ON DATABASE order_db TO postgres;
    GRANT ALL PRIVILEGES ON DATABASE payment_db TO postgres;
---
apiVersion: apps/v1
kind: StatefulSet
metadata:
  name: postgres
  namespace: nexus-commerce
spec:
  serviceName: postgres-service
  replicas: 1
  selector:
    matchLabels:
      app: postgres
  template:
    metadata:
      labels:
        app: postgres
    spec:
      containers:
        - name: postgres
          image: postgres:16-alpine
          ports:
            - containerPort: 5432
              name: postgres
          env:
            - name: POSTGRES_USER
              valueFrom:
                secretKeyRef:
                  name: nexus-db-secrets
                  key: POSTGRES_USER
            - name: POSTGRES_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: nexus-db-secrets
                  key: POSTGRES_PASSWORD
          volumeMounts:
            - name: postgres-storage
              mountPath: /var/lib/postgresql/data
            - name: init-script
              mountPath: /docker-entrypoint-initdb.d/init-db.sql
              subPath: init-db.sql
          resources:
            requests:
              memory: "256Mi"
              cpu: "100m"
            limits:
              memory: "512Mi"
              cpu: "500m"
      volumes:
        - name: init-script
          configMap:
            name: postgres-init-script
  volumeClaimTemplates:
    - metadata:
        name: postgres-storage
      spec:
        accessModes: [ "ReadWriteOnce" ]
        resources:
          requests:
            storage: 2Gi
---
apiVersion: v1
kind: Service
metadata:
  name: postgres-service
  namespace: nexus-commerce
spec:
  ports:
    - port: 5432
      targetPort: 5432
      name: postgres
  selector:
    app: postgres
EOF_FILE_11
echo "  [+] Created k8s/02-postgres.yaml"

mkdir -p "k8s"
cat <<'EOF_FILE_12' > "k8s/03-kafka.yaml"
apiVersion: apps/v1
kind: Deployment
metadata:
  name: kafka
  namespace: nexus-commerce
spec:
  replicas: 1
  selector:
    matchLabels:
      app: kafka
  template:
    metadata:
      labels:
        app: kafka
    spec:
      containers:
        - name: kafka
          image: apache/kafka:3.8.0
          ports:
            - containerPort: 9092
              name: kafka-client
            - containerPort: 9093
              name: kafka-ctrl
          env:
            - name: KAFKA_NODE_ID
              value: "1"
            - name: KAFKA_PROCESS_ROLES
              value: "broker,controller"
            - name: KAFKA_LISTENERS
              value: "PLAINTEXT://:9092,CONTROLLER://:9093"
            - name: KAFKA_ADVERTISED_LISTENERS
              value: "PLAINTEXT://kafka-service.nexus-commerce.svc.cluster.local:9092"
            - name: KAFKA_CONTROLLER_LISTENER_NAMES
              value: "CONTROLLER"
            - name: KAFKA_LISTENER_SECURITY_PROTOCOL_MAP
              value: "CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT"
            - name: KAFKA_CONTROLLER_QUORUM_VOTERS
              value: "1@localhost:9093"
            - name: KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR
              value: "1"
            - name: KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR
              value: "1"
            - name: KAFKA_TRANSACTION_STATE_LOG_MIN_ISR
              value: "1"
            - name: KAFKA_GROUP_INITIAL_REBALANCE_DELAY_MS
              value: "0"
            - name: KAFKA_NUM_PARTITIONS
              value: "3"
          resources:
            requests:
              memory: "512Mi"
              cpu: "200m"
            limits:
              memory: "1Gi"
              cpu: "1000m"
---
apiVersion: v1
kind: Service
metadata:
  name: kafka-service
  namespace: nexus-commerce
spec:
  ports:
    - port: 9092
      targetPort: 9092
      name: kafka-client
  selector:
    app: kafka
EOF_FILE_12
echo "  [+] Created k8s/03-kafka.yaml"

mkdir -p "k8s"
cat <<'EOF_FILE_13' > "k8s/04-eureka.yaml"
apiVersion: apps/v1
kind: Deployment
metadata:
  name: eureka-server
  namespace: nexus-commerce
spec:
  replicas: 1
  selector:
    matchLabels:
      app: eureka-server
  template:
    metadata:
      labels:
        app: eureka-server
    spec:
      containers:
        - name: eureka-server
          image: ghcr.io/nexus-commerce/eureka-server:latest
          imagePullPolicy: IfNotPresent
          ports:
            - containerPort: 8761
              name: http
          env:
            - name: EUREKA_INSTANCE_HOSTNAME
              value: "eureka-server.nexus-commerce.svc.cluster.local"
            - name: EUREKA_CLIENT_REGISTER_WITH_EUREKA
              value: "false"
            - name: EUREKA_CLIENT_FETCH_REGISTRY
              value: "false"
          readinessProbe:
            httpGet:
              path: /actuator/health
              port: 8761
            initialDelaySeconds: 20
            periodSeconds: 10
          livenessProbe:
            httpGet:
              path: /actuator/health
              port: 8761
            initialDelaySeconds: 30
            periodSeconds: 15
          resources:
            requests:
              memory: "256Mi"
              cpu: "100m"
            limits:
              memory: "512Mi"
              cpu: "500m"
---
apiVersion: v1
kind: Service
metadata:
  name: eureka-server
  namespace: nexus-commerce
spec:
  ports:
    - port: 8761
      targetPort: 8761
      name: http
  selector:
    app: eureka-server
EOF_FILE_13
echo "  [+] Created k8s/04-eureka.yaml"

mkdir -p "k8s"
cat <<'EOF_FILE_14' > "k8s/05-gateway.yaml"
apiVersion: apps/v1
kind: Deployment
metadata:
  name: api-gateway
  namespace: nexus-commerce
spec:
  replicas: 2
  selector:
    matchLabels:
      app: api-gateway
  template:
    metadata:
      labels:
        app: api-gateway
    spec:
      containers:
        - name: api-gateway
          image: ghcr.io/nexus-commerce/api-gateway:latest
          imagePullPolicy: IfNotPresent
          ports:
            - containerPort: 8080
              name: http
          env:
            - name: EUREKA_CLIENT_SERVICEURL_DEFAULTZONE
              valueFrom:
                configMapKeyRef:
                  name: nexus-global-config
                  key: EUREKA_SERVER_URL
            - name: EUREKA_INSTANCE_IP_ADDRESS
              valueFrom:
                fieldRef:
                  fieldPath: status.podIP
            - name: EUREKA_INSTANCE_HOSTNAME
              valueFrom:
                fieldRef:
                  fieldPath: status.podIP
          readinessProbe:
            httpGet:
              path: /actuator/health
              port: 8080
            initialDelaySeconds: 20
            periodSeconds: 10
          livenessProbe:
            httpGet:
              path: /actuator/health
              port: 8080
            initialDelaySeconds: 30
            periodSeconds: 15
          resources:
            requests:
              memory: "256Mi"
              cpu: "150m"
            limits:
              memory: "512Mi"
              cpu: "500m"
---
apiVersion: v1
kind: Service
metadata:
  name: api-gateway
  namespace: nexus-commerce
spec:
  type: LoadBalancer
  ports:
    - port: 8080
      targetPort: 8080
      name: http
  selector:
    app: api-gateway
EOF_FILE_14
echo "  [+] Created k8s/05-gateway.yaml"

mkdir -p "k8s"
cat <<'EOF_FILE_15' > "k8s/06-microservices.yaml"
# ==============================================================================
# Domain Microservices: Product, Inventory, Order, Payment, Notification, AI
# ==============================================================================

# --- 1. Product Service ---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: product-service
  namespace: nexus-commerce
spec:
  replicas: 2
  selector:
    matchLabels:
      app: product-service
  template:
    metadata:
      labels:
        app: product-service
    spec:
      containers:
        - name: product-service
          image: ghcr.io/nexus-commerce/product-service:latest
          imagePullPolicy: IfNotPresent
          ports:
            - containerPort: 8081
          env:
            - name: SPRING_DATASOURCE_URL
              value: "jdbc:postgresql://postgres-service.nexus-commerce.svc.cluster.local:5432/product_db"
            - name: SPRING_DATASOURCE_USERNAME
              valueFrom:
                secretKeyRef:
                  name: nexus-db-secrets
                  key: POSTGRES_USER
            - name: SPRING_DATASOURCE_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: nexus-db-secrets
                  key: POSTGRES_PASSWORD
            - name: EUREKA_CLIENT_SERVICEURL_DEFAULTZONE
              valueFrom:
                configMapKeyRef:
                  name: nexus-global-config
                  key: EUREKA_SERVER_URL
            - name: EUREKA_INSTANCE_IP_ADDRESS
              valueFrom:
                fieldRef:
                  fieldPath: status.podIP
            - name: EUREKA_INSTANCE_HOSTNAME
              valueFrom:
                fieldRef:
                  fieldPath: status.podIP
          resources:
            requests:
              memory: "256Mi"
              cpu: "150m"
            limits:
              memory: "512Mi"
              cpu: "500m"
---
apiVersion: v1
kind: Service
metadata:
  name: product-service
  namespace: nexus-commerce
spec:
  ports:
    - port: 8081
      targetPort: 8081
  selector:
    app: product-service
---
# --- 2. Inventory Service ---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: inventory-service
  namespace: nexus-commerce
spec:
  replicas: 2
  selector:
    matchLabels:
      app: inventory-service
  template:
    metadata:
      labels:
        app: inventory-service
    spec:
      containers:
        - name: inventory-service
          image: ghcr.io/nexus-commerce/inventory-service:latest
          imagePullPolicy: IfNotPresent
          ports:
            - containerPort: 8082
          env:
            - name: SPRING_DATASOURCE_URL
              value: "jdbc:postgresql://postgres-service.nexus-commerce.svc.cluster.local:5432/inventory_db"
            - name: SPRING_DATASOURCE_USERNAME
              valueFrom:
                secretKeyRef:
                  name: nexus-db-secrets
                  key: POSTGRES_USER
            - name: SPRING_DATASOURCE_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: nexus-db-secrets
                  key: POSTGRES_PASSWORD
            - name: EUREKA_CLIENT_SERVICEURL_DEFAULTZONE
              valueFrom:
                configMapKeyRef:
                  name: nexus-global-config
                  key: EUREKA_SERVER_URL
            - name: EUREKA_INSTANCE_IP_ADDRESS
              valueFrom:
                fieldRef:
                  fieldPath: status.podIP
            - name: EUREKA_INSTANCE_HOSTNAME
              valueFrom:
                fieldRef:
                  fieldPath: status.podIP
          resources:
            requests:
              memory: "256Mi"
              cpu: "150m"
            limits:
              memory: "512Mi"
              cpu: "500m"
---
apiVersion: v1
kind: Service
metadata:
  name: inventory-service
  namespace: nexus-commerce
spec:
  ports:
    - port: 8082
      targetPort: 8082
  selector:
    app: inventory-service
---
# --- 3. Order Service ---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: order-service
  namespace: nexus-commerce
spec:
  replicas: 2
  selector:
    matchLabels:
      app: order-service
  template:
    metadata:
      labels:
        app: order-service
    spec:
      containers:
        - name: order-service
          image: ghcr.io/nexus-commerce/order-service:latest
          imagePullPolicy: IfNotPresent
          ports:
            - containerPort: 8083
          env:
            - name: SPRING_DATASOURCE_URL
              value: "jdbc:postgresql://postgres-service.nexus-commerce.svc.cluster.local:5432/order_db"
            - name: SPRING_DATASOURCE_USERNAME
              valueFrom:
                secretKeyRef:
                  name: nexus-db-secrets
                  key: POSTGRES_USER
            - name: SPRING_DATASOURCE_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: nexus-db-secrets
                  key: POSTGRES_PASSWORD
            - name: SPRING_KAFKA_BOOTSTRAP_SERVERS
              valueFrom:
                configMapKeyRef:
                  name: nexus-global-config
                  key: KAFKA_BOOTSTRAP_SERVERS
            - name: EUREKA_CLIENT_SERVICEURL_DEFAULTZONE
              valueFrom:
                configMapKeyRef:
                  name: nexus-global-config
                  key: EUREKA_SERVER_URL
            - name: EUREKA_INSTANCE_IP_ADDRESS
              valueFrom:
                fieldRef:
                  fieldPath: status.podIP
            - name: EUREKA_INSTANCE_HOSTNAME
              valueFrom:
                fieldRef:
                  fieldPath: status.podIP
          resources:
            requests:
              memory: "256Mi"
              cpu: "150m"
            limits:
              memory: "512Mi"
              cpu: "500m"
---
apiVersion: v1
kind: Service
metadata:
  name: order-service
  namespace: nexus-commerce
spec:
  ports:
    - port: 8083
      targetPort: 8083
  selector:
    app: order-service
---
# --- 4. Payment Service ---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: payment-service
  namespace: nexus-commerce
spec:
  replicas: 2
  selector:
    matchLabels:
      app: payment-service
  template:
    metadata:
      labels:
        app: payment-service
    spec:
      containers:
        - name: payment-service
          image: ghcr.io/nexus-commerce/payment-service:latest
          imagePullPolicy: IfNotPresent
          ports:
            - containerPort: 8084
          env:
            - name: SPRING_DATASOURCE_URL
              value: "jdbc:postgresql://postgres-service.nexus-commerce.svc.cluster.local:5432/payment_db"
            - name: SPRING_DATASOURCE_USERNAME
              valueFrom:
                secretKeyRef:
                  name: nexus-db-secrets
                  key: POSTGRES_USER
            - name: SPRING_DATASOURCE_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: nexus-db-secrets
                  key: POSTGRES_PASSWORD
            - name: SPRING_KAFKA_BOOTSTRAP_SERVERS
              valueFrom:
                configMapKeyRef:
                  name: nexus-global-config
                  key: KAFKA_BOOTSTRAP_SERVERS
            - name: EUREKA_CLIENT_SERVICEURL_DEFAULTZONE
              valueFrom:
                configMapKeyRef:
                  name: nexus-global-config
                  key: EUREKA_SERVER_URL
            - name: EUREKA_INSTANCE_IP_ADDRESS
              valueFrom:
                fieldRef:
                  fieldPath: status.podIP
            - name: EUREKA_INSTANCE_HOSTNAME
              valueFrom:
                fieldRef:
                  fieldPath: status.podIP
          resources:
            requests:
              memory: "256Mi"
              cpu: "150m"
            limits:
              memory: "512Mi"
              cpu: "500m"
---
apiVersion: v1
kind: Service
metadata:
  name: payment-service
  namespace: nexus-commerce
spec:
  ports:
    - port: 8084
      targetPort: 8084
  selector:
    app: payment-service
---
# --- 5. Notification Service ---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: notification-service
  namespace: nexus-commerce
spec:
  replicas: 1
  selector:
    matchLabels:
      app: notification-service
  template:
    metadata:
      labels:
        app: notification-service
    spec:
      containers:
        - name: notification-service
          image: ghcr.io/nexus-commerce/notification-service:latest
          imagePullPolicy: IfNotPresent
          ports:
            - containerPort: 8085
          env:
            - name: SPRING_KAFKA_BOOTSTRAP_SERVERS
              valueFrom:
                configMapKeyRef:
                  name: nexus-global-config
                  key: KAFKA_BOOTSTRAP_SERVERS
            - name: EUREKA_CLIENT_SERVICEURL_DEFAULTZONE
              valueFrom:
                configMapKeyRef:
                  name: nexus-global-config
                  key: EUREKA_SERVER_URL
            - name: EUREKA_INSTANCE_IP_ADDRESS
              valueFrom:
                fieldRef:
                  fieldPath: status.podIP
            - name: EUREKA_INSTANCE_HOSTNAME
              valueFrom:
                fieldRef:
                  fieldPath: status.podIP
          resources:
            requests:
              memory: "256Mi"
              cpu: "100m"
            limits:
              memory: "512Mi"
              cpu: "500m"
---
apiVersion: v1
kind: Service
metadata:
  name: notification-service
  namespace: nexus-commerce
spec:
  ports:
    - port: 8085
      targetPort: 8085
  selector:
    app: notification-service
---
# --- 6. AI Compliance Service ---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: ai-compliance-service
  namespace: nexus-commerce
spec:
  replicas: 1
  selector:
    matchLabels:
      app: ai-compliance-service
  template:
    metadata:
      labels:
        app: ai-compliance-service
    spec:
      containers:
        - name: ai-compliance-service
          image: ghcr.io/nexus-commerce/ai-compliance-service:latest
          imagePullPolicy: IfNotPresent
          ports:
            - containerPort: 8086
          env:
            - name: EUREKA_CLIENT_SERVICEURL_DEFAULTZONE
              valueFrom:
                configMapKeyRef:
                  name: nexus-global-config
                  key: EUREKA_SERVER_URL
            - name: EUREKA_INSTANCE_IP_ADDRESS
              valueFrom:
                fieldRef:
                  fieldPath: status.podIP
            - name: EUREKA_INSTANCE_HOSTNAME
              valueFrom:
                fieldRef:
                  fieldPath: status.podIP
          resources:
            requests:
              memory: "256Mi"
              cpu: "100m"
            limits:
              memory: "512Mi"
              cpu: "500m"
---
apiVersion: v1
kind: Service
metadata:
  name: ai-compliance-service
  namespace: nexus-commerce
spec:
  ports:
    - port: 8086
      targetPort: 8086
  selector:
    app: ai-compliance-service
EOF_FILE_15
echo "  [+] Created k8s/06-microservices.yaml"

mkdir -p "k8s"
cat <<'EOF_FILE_16' > "k8s/kustomization.yaml"
apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization
namespace: nexus-commerce

resources:
  - 00-namespace.yaml
  - 01-configmap-secrets.yaml
  - 02-postgres.yaml
  - 03-kafka.yaml
  - 04-eureka.yaml
  - 05-gateway.yaml
  - 06-microservices.yaml
EOF_FILE_16
echo "  [+] Created k8s/kustomization.yaml"

mkdir -p "helm/nexus-commerce"
cat <<'EOF_FILE_17' > "helm/nexus-commerce/Chart.yaml"
apiVersion: v2
name: nexus-commerce
description: Production Helm Chart for Nexus Commerce Cloud Native Microservices Platform
type: application
version: 1.0.0
appVersion: "1.0.0"
keywords:
  - spring-boot
  - microservices
  - ecommerce
  - kafka
  - eureka
maintainers:
  - name: Nexus Commerce Architecture Team
EOF_FILE_17
echo "  [+] Created helm/nexus-commerce/Chart.yaml"

mkdir -p "helm/nexus-commerce"
cat <<'EOF_FILE_18' > "helm/nexus-commerce/values.yaml"
global:
  imageRegistry: ghcr.io
  imageRepositoryPrefix: nexus-commerce
  imageTag: latest
  imagePullPolicy: IfNotPresent
  namespace: nexus-commerce

eureka:
  replicaCount: 1
  port: 8761
  resources:
    limits:
      cpu: 500m
      memory: 512Mi
    requests:
      cpu: 100m
      memory: 256Mi

gateway:
  replicaCount: 2
  port: 8080
  serviceType: LoadBalancer
  resources:
    limits:
      cpu: 500m
      memory: 512Mi
    requests:
      cpu: 150m
      memory: 256Mi

services:
  product:
    port: 8081
    replicaCount: 2
    dbName: product_db
  inventory:
    port: 8082
    replicaCount: 2
    dbName: inventory_db
  order:
    port: 8083
    replicaCount: 2
    dbName: order_db
  payment:
    port: 8084
    replicaCount: 2
    dbName: payment_db
  notification:
    port: 8085
    replicaCount: 1
  compliance:
    port: 8086
    replicaCount: 1

postgres:
  enabled: true
  storage: 2Gi
  user: postgres
  password: "postgres_secure_password_2026"

kafka:
  enabled: true
  replicaCount: 1
  partitions: 3
EOF_FILE_18
echo "  [+] Created helm/nexus-commerce/values.yaml"

mkdir -p "helm/nexus-commerce/templates"
cat <<'EOF_FILE_19' > "helm/nexus-commerce/templates/_helpers.tpl"
{{/*
Expand the name of the chart.
*/}}
{{- define "nexus-commerce.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Common labels
*/}}
{{- define "nexus-commerce.labels" -}}
helm.sh/chart: {{ include "nexus-commerce.name" . }}
app.kubernetes.io/name: {{ include "nexus-commerce.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}
EOF_FILE_19
echo "  [+] Created helm/nexus-commerce/templates/_helpers.tpl"

mkdir -p "helm/nexus-commerce/templates"
cat <<'EOF_FILE_20' > "helm/nexus-commerce/templates/configmap.yaml"
apiVersion: v1
kind: ConfigMap
metadata:
  name: {{ include "nexus-commerce.name" . }}-config
  labels:
    {{- include "nexus-commerce.labels" . | nindent 4 }}
data:
  EUREKA_SERVER_URL: "http://eureka-server:{{ .Values.eureka.port }}/eureka/"
  KAFKA_BOOTSTRAP_SERVERS: "kafka-service:9092"
  POSTGRES_HOST: "postgres-service"
  POSTGRES_PORT: "5432"
---
apiVersion: v1
kind: Secret
metadata:
  name: {{ include "nexus-commerce.name" . }}-db-secret
  labels:
    {{- include "nexus-commerce.labels" . | nindent 4 }}
type: Opaque
stringData:
  POSTGRES_USER: {{ .Values.postgres.user | quote }}
  POSTGRES_PASSWORD: {{ .Values.postgres.password | quote }}
EOF_FILE_20
echo "  [+] Created helm/nexus-commerce/templates/configmap.yaml"

mkdir -p "helm/nexus-commerce/templates"
cat <<'EOF_FILE_21' > "helm/nexus-commerce/templates/eureka.yaml"
apiVersion: apps/v1
kind: Deployment
metadata:
  name: eureka-server
spec:
  replicas: {{ .Values.eureka.replicaCount }}
  selector:
    matchLabels:
      app: eureka-server
  template:
    metadata:
      labels:
        app: eureka-server
    spec:
      containers:
        - name: eureka-server
          image: "{{ .Values.global.imageRegistry }}/{{ .Values.global.imageRepositoryPrefix }}/eureka-server:{{ .Values.global.imageTag }}"
          imagePullPolicy: {{ .Values.global.imagePullPolicy }}
          ports:
            - containerPort: {{ .Values.eureka.port }}
          env:
            - name: EUREKA_INSTANCE_HOSTNAME
              value: "eureka-server"
            - name: EUREKA_CLIENT_REGISTER_WITH_EUREKA
              value: "false"
            - name: EUREKA_CLIENT_FETCH_REGISTRY
              value: "false"
          resources:
            {{- toYaml .Values.eureka.resources | nindent 12 }}
---
apiVersion: v1
kind: Service
metadata:
  name: eureka-server
spec:
  ports:
    - port: {{ .Values.eureka.port }}
      targetPort: {{ .Values.eureka.port }}
  selector:
    app: eureka-server
EOF_FILE_21
echo "  [+] Created helm/nexus-commerce/templates/eureka.yaml"

mkdir -p "helm/nexus-commerce/templates"
cat <<'EOF_FILE_22' > "helm/nexus-commerce/templates/gateway.yaml"
apiVersion: apps/v1
kind: Deployment
metadata:
  name: api-gateway
spec:
  replicas: {{ .Values.gateway.replicaCount }}
  selector:
    matchLabels:
      app: api-gateway
  template:
    metadata:
      labels:
        app: api-gateway
    spec:
      containers:
        - name: api-gateway
          image: "{{ .Values.global.imageRegistry }}/{{ .Values.global.imageRepositoryPrefix }}/api-gateway:{{ .Values.global.imageTag }}"
          imagePullPolicy: {{ .Values.global.imagePullPolicy }}
          ports:
            - containerPort: {{ .Values.gateway.port }}
          env:
            - name: EUREKA_CLIENT_SERVICEURL_DEFAULTZONE
              valueFrom:
                configMapKeyRef:
                  name: {{ include "nexus-commerce.name" . }}-config
                  key: EUREKA_SERVER_URL
            - name: EUREKA_INSTANCE_IP_ADDRESS
              valueFrom:
                fieldRef:
                  fieldPath: status.podIP
            - name: EUREKA_INSTANCE_HOSTNAME
              valueFrom:
                fieldRef:
                  fieldPath: status.podIP
          resources:
            {{- toYaml .Values.gateway.resources | nindent 12 }}
---
apiVersion: v1
kind: Service
metadata:
  name: api-gateway
spec:
  type: {{ .Values.gateway.serviceType }}
  ports:
    - port: {{ .Values.gateway.port }}
      targetPort: {{ .Values.gateway.port }}
  selector:
    app: api-gateway
EOF_FILE_22
echo "  [+] Created helm/nexus-commerce/templates/gateway.yaml"

mkdir -p "helm/nexus-commerce/templates"
cat <<'EOF_FILE_23' > "helm/nexus-commerce/templates/kafka.yaml"
{{- if .Values.kafka.enabled }}
apiVersion: apps/v1
kind: Deployment
metadata:
  name: kafka
spec:
  replicas: {{ .Values.kafka.replicaCount }}
  selector:
    matchLabels:
      app: kafka
  template:
    metadata:
      labels:
        app: kafka
    spec:
      containers:
        - name: kafka
          image: apache/kafka:3.8.0
          ports:
            - containerPort: 9092
              name: kafka-client
            - containerPort: 9093
              name: kafka-ctrl
          env:
            - name: KAFKA_NODE_ID
              value: "1"
            - name: KAFKA_PROCESS_ROLES
              value: "broker,controller"
            - name: KAFKA_LISTENERS
              value: "PLAINTEXT://:9092,CONTROLLER://:9093"
            - name: KAFKA_ADVERTISED_LISTENERS
              value: "PLAINTEXT://kafka-service:9092"
            - name: KAFKA_CONTROLLER_LISTENER_NAMES
              value: "CONTROLLER"
            - name: KAFKA_LISTENER_SECURITY_PROTOCOL_MAP
              value: "CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT"
            - name: KAFKA_CONTROLLER_QUORUM_VOTERS
              value: "1@localhost:9093"
            - name: KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR
              value: "1"
            - name: KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR
              value: "1"
            - name: KAFKA_TRANSACTION_STATE_LOG_MIN_ISR
              value: "1"
            - name: KAFKA_NUM_PARTITIONS
              value: "{{ .Values.kafka.partitions }}"
---
apiVersion: v1
kind: Service
metadata:
  name: kafka-service
spec:
  ports:
    - port: 9092
      targetPort: 9092
  selector:
    app: kafka
{{- end }}
EOF_FILE_23
echo "  [+] Created helm/nexus-commerce/templates/kafka.yaml"

mkdir -p "helm/nexus-commerce/templates"
cat <<'EOF_FILE_24' > "helm/nexus-commerce/templates/postgres.yaml"
{{- if .Values.postgres.enabled }}
apiVersion: v1
kind: ConfigMap
metadata:
  name: {{ include "nexus-commerce.name" . }}-postgres-init
data:
  init-db.sql: |
    CREATE DATABASE product_db;
    CREATE DATABASE inventory_db;
    CREATE DATABASE order_db;
    CREATE DATABASE payment_db;
    GRANT ALL PRIVILEGES ON DATABASE product_db TO {{ .Values.postgres.user }};
    GRANT ALL PRIVILEGES ON DATABASE inventory_db TO {{ .Values.postgres.user }};
    GRANT ALL PRIVILEGES ON DATABASE order_db TO {{ .Values.postgres.user }};
    GRANT ALL PRIVILEGES ON DATABASE payment_db TO {{ .Values.postgres.user }};
---
apiVersion: apps/v1
kind: StatefulSet
metadata:
  name: postgres
spec:
  serviceName: postgres-service
  replicas: 1
  selector:
    matchLabels:
      app: postgres
  template:
    metadata:
      labels:
        app: postgres
    spec:
      containers:
        - name: postgres
          image: postgres:16-alpine
          ports:
            - containerPort: 5432
              name: postgres
          env:
            - name: POSTGRES_USER
              valueFrom:
                secretKeyRef:
                  name: {{ include "nexus-commerce.name" . }}-db-secret
                  key: POSTGRES_USER
            - name: POSTGRES_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: {{ include "nexus-commerce.name" . }}-db-secret
                  key: POSTGRES_PASSWORD
          volumeMounts:
            - name: postgres-storage
              mountPath: /var/lib/postgresql/data
            - name: init-script
              mountPath: /docker-entrypoint-initdb.d/init-db.sql
              subPath: init-db.sql
      volumes:
        - name: init-script
          configMap:
            name: {{ include "nexus-commerce.name" . }}-postgres-init
  volumeClaimTemplates:
    - metadata:
        name: postgres-storage
      spec:
        accessModes: [ "ReadWriteOnce" ]
        resources:
          requests:
            storage: {{ .Values.postgres.storage }}
---
apiVersion: v1
kind: Service
metadata:
  name: postgres-service
spec:
  ports:
    - port: 5432
      targetPort: 5432
  selector:
    app: postgres
{{- end }}
EOF_FILE_24
echo "  [+] Created helm/nexus-commerce/templates/postgres.yaml"

echo ""
echo "======================================================================"
echo " All CI/CD, Kubernetes & Helm manifests successfully generated!"
echo " Next steps:"
echo "   1. git add . && git commit -m 'Add CI/CD pipeline, Dockerfiles, K8s & Helm'"
echo "   2. git push origin main"
echo "   3. Test locally: kubectl apply -k k8s/"
echo "======================================================================"