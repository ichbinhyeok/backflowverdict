@import owner.backflow.data.model.product.ModelMerchantLink
@param java.util.List<ModelMerchantLink> merchants
@param String partNumber

@if (!merchants.isEmpty())
    <div class="bv-merchant-links"><span>Retailer listing</span>
        @for (var merchant : merchants)
            <a href="${merchant.url()}" data-link-kind="merchant" data-part-number="${partNumber}" rel="noopener noreferrer">Check ${partNumber} at ${merchant.seller()} &rarr;</a>
            <small>Listing checked ${merchant.lastVerified()}. Confirm fit and current stock with the seller.</small>
        @endfor
        <small>Ordinary retailer link; BackflowVerdict receives no commission from this link. Manufacturer documents govern compatibility.</small>
    </div>
@endif
