# Shopcomeback

Paper tabanlı Minecraft sunucuları için geliştirilmiş mağaza eklentisi.

> Bu README bir taslaktır. `[...]` ile işaretli yerleri kendi eklentine göre değiştir.

## Özellikler

- [Özellik 1, örn. oyuncular için mağaza menüsü]
- [Özellik 2, örn. eşya alma ve satma]
- [Özellik 3, örn. ayarlanabilir fiyatlar]
- Ayarlar `config.yml` dosyasından düzenlenir

## Gereksinimler

| Gereken | Sürüm |
|---|---|
| Sunucu | Paper 1.21.x |
| Java | 21 veya üzeri |
| Bağımlılıklar | [Yok / Vault / vb.] |

## Kurulum

1. `Shopcomeback.jar` dosyasını indir.
2. Dosyayı sunucunun `plugins/` klasörüne koy.
3. Sunucuyu yeniden başlat.
4. `plugins/Shopcomeback/config.yml` dosyasını istediğin gibi düzenle.
5. Oyunda ya da konsolda `/shopcomeback reload` yaz.

## Komutlar

| Komut | Açıklama | Yetki |
|---|---|---|
| `/shopcomeback` | Ana menüyü açar | `shopcomeback.use` |
| `/shopcomeback reload` | Ayarları yeniden yükler | `shopcomeback.admin` |
| `/shopcomeback help` | Komut listesini gösterir | `shopcomeback.use` |

## Yetkiler

| Yetki | Açıklama | Varsayılan |
|---|---|---|
| `shopcomeback.use` | Eklentiyi kullanma | Herkes |
| `shopcomeback.admin` | Yönetici komutları | OP |

## Ayarlar

`config.yml` örneği:

```yaml
# Örnek, kendi ayarlarınla değiştir
prefix: "&6[Shop] &r"
language: tr
```

## Derleme

Kaynak koddan derlemek için:

```bash
git clone [depo adresi]
cd shopcomeback
mvn clean package
```

Derlenen dosya `target/` klasöründe çıkar.

## Sorun giderme

- **Eklenti yüklenmiyor:** Java 21 kullandığından ve Paper sürümünün uyumlu olduğundan emin ol.
- **Komutlar çalışmıyor:** Yetkileri kontrol et.
- **Hata mesajı alıyorsun:** `logs/latest.log` dosyasındaki hata metniyle birlikte bildir.

## Katkı

Hata bildirimi ve öneriler için [iletişim / depo adresi] üzerinden ulaşabilirsin.

## Lisans

[MIT / Tüm hakları saklıdır / vb.]
```
