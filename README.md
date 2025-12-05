# 🚀 Enterprise Microservice Gateway (EMG) Projesi

Bu proje, bir e-ticaret altyapısının temel servislerini modern, dağıtık ve güvenli bir yaklaşımla hayata geçirmek amacıyla **Java 17** ve **Spring Cloud** ekosistemi kullanılarak geliştirilmiştir. Projenin ana hedefi, geleneksel monolitik yapıların sınırlamalarından kurtularak **ölçeklenebilirliği**, **esnekliği** ve **gözlemlenebilirliği** en üst düzeye çıkarmaktır.

## 1. Proje Genel Bakış: Bağımsız Servisler ve Merkezi Yönetim

Proje, temel olarak **Kullanıcı Hesapları (`Account`)** ve **Ürün Katalogu (`Product`)** işlevlerini üstlenen iki bağımsız mikroservisten oluşur. Bu yapının temel amacı, sistemde **hata yalıtımı (fault isolation)** sağlamaktır; yani bir serviste yaşanan aksaklığın tüm sistemi durdurmasını engellemektir. Sisteme dış dünyadan gelen tüm trafik, merkezi bir kapı görevi gören **API Gateway** üzerinden yönlendirilir. Gateway, isteğin güvenliğini doğruladıktan sonra, isteği dinamik olarak ilgili arka uç servisine iletir. Bu tasarım, mimarinin temelini esneklik ve direnç üzerine kurar.


![Enterprise Mikroservis Mimari Diyagramı](assets/emg-diagram.png)

***

## 2. Mimari Derinlik: Bağımsızlık ve Esneklik

Bu bölümde, projenin dağıtık sistemler prensiplerine uygunluğunu gösteren temel yapısal kararlar incelenmektedir.

### 2.1. Hizmet Keşfi (Service Discovery)
Mikroservis mimarisinin temelini oluşturan **Hizmet Keşfi** için **Spring Cloud Eureka** kullanılmıştır. Her servis (Account, Product) başladığında kendini Eureka Server'a kaydeder. Bu mekanizma sayesinde, **API Gateway** bir istek yönlendirirken servisin anlık olarak hangi adreste çalıştığını bilmek zorunda kalmaz, sadece Eureka'ya sorar. Bu dinamik adres çözümü, IP adresi bağımlılığını ortadan kaldırarak mimariye doğal bir **esneklik** ve **otomatik yük dengeleme** yeteneği kazandırır.

### 2.2. Veri Bağımsızlığı: Her Servisin Kendi Veritabanı
Projenin en önemli mimari kararlarından biri, **Veri Bağımsızlığı** ilkesidir. Geleneksel yaklaşımların aksine, her bir mikroservis yalnızca kendi verisinden sorumludur. Bu, **Account Service** için ayrı bir PostgreSQL konteyneri (`postgres-db`) ve **Product Service** için ayrı bir PostgreSQL konteyneri (`product-db`) tanımlanarak hayata geçirilmiştir. Bu ayrım, servisler arasında **gevşek bağlantı (loose coupling)** sağlarken, olası bir veritabanı değişikliğinde diğer servisin etkilenmemesini garanti eder ve veri yükünün tek bir noktada toplanmasını önler.

***

## 3. Güvenlik Mekanizmaları: Ön ve Arka Cephe Savunması

Dağıtık sistemlerde kritik öneme sahip olan güvenlik, iki aşamalı bir strateji ile sağlanmıştır:

### 3.1. Ön Cephe Güvenliği (API Gateway)
Tüm kullanıcı istekleri için merkezi kimlik doğrulama, **Spring Cloud Gateway** üzerinde **JWT (JSON Web Token)** kullanılarak uygulanmıştır. Kullanıcı giriş yaptıktan sonra aldığı JWT'nin geçerliliği ve süresi Gateway seviyesinde kontrol edilir. Ayrıca, kullanıcı **çıkış yaptığında (Logout)**, token süresi dolmamış olsa bile **Redis** üzerinde anında kara listeye alınır. Bu *token blacklisting* mekanizması, oturum sonlandırma işlemlerinin anında gerçekleşmesini sağlayarak güvenlik zafiyetlerini minimuma indirir.

### 3.2. Arka Cephe Güvenliği (İç Savunma)
Servisler arası iletişimin güvenliğini sağlamak amacıyla, mikroservislerin dış dünyadan doğrudan erişimi engellenmiştir. Gateway, yönlendirdiği isteklere özel bir gizli anahtar olan **`X-Internal-Secret`** başlığını ekler. Her arka uç servis, gelen isteği işleme almadan önce bu anahtarı kontrol eden bir **`InternalAccessFilter`** kullanır. Bu filtre, yalnızca **güvenilir API Gateway**'den gelen isteklere yanıt verilmesini garantiler ve sisteme ek bir iç güvenlik katmanı sağlar.

***

## 4. Gözlemlenebilirlik (Observability) ve Performans Analizi

Dağıtık bir sistemin sağlığını ve performansını anlık olarak izleme yeteneği, projenin operasyonel olgunluğunu gösterir.

* **Dağıtık İzleme (Tracing) - Zipkin:** Tüm servislerde **`MANAGEMENT_TRACING_ENABLED=true`** ayarı aktif edilmiştir. Bu sayede, bir API isteği birden fazla servisten geçtiğinde, isteğin tüm yaşam döngüsü tek bir kimlik (`Trace ID`) altında **Zipkin**'de izlenir. Bu, gecikme sürelerinin (latency) ve hata noktalarının saniyeler içinde tespit edilmesini sağlar.
* **Metrikler ve Görselleştirme - Prometheus & Grafana:** Servislerin CPU, bellek ve istek süresi gibi kritik metrikleri **Micrometer** aracılığıyla toplanır ve **Prometheus**'a sunulur. **Grafana** ise Prometheus'tan çektiği bu verileri kullanarak sistemin anlık sağlık durumunu ve performans trendlerini anlaşılır dashboard'lar üzerinden takip etme imkanı sunar.

## 5. Proje Ekosistemi ve Teknolojiler

Projede kullanılan temel teknolojiler, modern yazılım mühendisliği gereksinimlerini karşılamaktadır:

| Kategori | Teknoloji | Açıklama |
| :--- | :--- | :--- |
| **Geliştirme** | Java 17, Spring Boot 3, Maven | Kurumsal düzeyde hızlı uygulama geliştirme ortamı. |
| **Mikroservisler** | Spring Cloud Eureka, Spring Cloud Gateway | Dinamik keşif, merkezi yönlendirme ve yük dengeleme. |
| **Güvenlik** | Spring Security, JJWT, Redis | Oturum yönetimi ve hızlı token iptali. |
| **Veritabanı** | PostgreSQL, Spring Data JPA | Güvenilir ve ilişkisel veri yönetimi. |
| **Konteynerleştirme** | Docker, Docker Compose | Geliştirme/Test ortamını tek komutla kurma yeteneği. |
| **Gözlemlenebilirlik** | Zipkin, Prometheus, Grafana | Sistemin performans ve sağlık takibi. |

***

## 6. Kurulum ve Başlatma Kılavuzu

Proje, tüm bağımlılıkları (DB'ler, Redis, İzleme araçları) içerdiği için kurulumu **Docker Compose** ile basitleştirilmiştir.

### 6.1. Ön Gereksinimler

* JDK 17 veya üstü
* Apache Maven
* Docker ve Docker Compose

### 6.2. Başlatma Adımları

1.  **Kodları Derleyin:** Proje ana dizinine gidin ve tüm servisleri derleyin:
    ```bash
    mvn clean package -DskipTests
    ```
2.  **Sistemi Başlatın:** Güncel `docker-compose.yml` dosyasının bulunduğu dizinde başlatma komutunu çalıştırın:
    ```bash
    docker compose up --build -d
    ```

### 6.3. Bağlantı Noktaları (Endpoints)

| Bileşen | Adres | Amaç |
| :--- | :--- | :--- |
| **Tüm API İstekleri** | `http://localhost:8080` | API Gateway |
| **Hizmet Keşfi** | `http://localhost:8761` | Eureka Dashboard |
| **İzleme (Tracing)** | `http://localhost:9411` | Zipkin Arayüzü |
| **Metrikler (Grafana)** | `http://localhost:3000` | Görselleştirme Arayüzü |

### 6.4. Başarılı Başlangıç Kanıtı

Aşağıdaki ekran görüntüleri, projenizin hem Docker konteyner seviyesinde hem de uygulama (Hizmet Keşfi) seviyesinde başarılı bir şekilde çalışır durumda olduğunu göstermektedir.

---

#### 1. 🖥️ Docker Konteyner Durumları (`docker compose ps` Çıktısı)

Bu terminal çıktısı, tüm mikroservis ve altyapı konteynerlerinin (**`postgres-db`** ve **`product-db`** için **`healthy`**, diğer uygulamalar için **`up`** veya **`running`**) ayağa kalktığını ve Docker tarafından sorunsuz yönetildiğini kanıtlar.

![Docker Compose Servis Durumu Kanıtı](assets/docker-compose-ps-output.PNG)

---

#### 2. 🐳 Docker Desktop Dashboard (Genel Bakış)

Docker Desktop uygulaması ekran görüntüsü, tüm sistemin tek bir proje altında, yeşil veya mavi renkte **Running** (Çalışıyor) durumunda olduğunu görsel olarak teyit eder.

![Docker Desktop Çalışma Kanıtı](assets/docker-desktop-dashboard.PNG)

---

#### 3. 🟢 Eureka Servis Kaydı (Hizmet Keşfi Kanıtı)

Eureka Dashboard ekranı, mikroservislerinizin (Gateway, Account, Product) başarılı bir şekilde merkezi kayıt defterine kaydolduğunu ve **`UP`** (Ayakta) durumunda olduğunu göstererek **Hizmet Keşfi** mekanizmasının doğru çalıştığını kanıtlar.

![Eureka Server Kayıtlı Servisler](assets/eureka-dashboard.png)