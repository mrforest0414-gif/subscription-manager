package com.example.submanager.web;

import com.example.submanager.subscription.BillingCycle;
import com.example.submanager.subscription.Category;
import com.example.submanager.subscription.Currency;
import com.example.submanager.subscription.PaymentStatus;
import com.example.submanager.subscription.SortKey;
import com.example.submanager.subscription.Subscription;
import com.example.submanager.subscription.SubscriptionForm;
import com.example.submanager.subscription.SubscriptionService;
import com.example.submanager.subscription.Summary;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
public class SubscriptionController {

    private final SubscriptionService service;

    public SubscriptionController(SubscriptionService service) {
        this.service = service;
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/subscriptions";
    }

    /** ダッシュボード(指標カード + 内訳グラフ + 一覧) */
    @GetMapping("/subscriptions")
    public String dashboard(@RequestParam(required = false) Category category,
                            @RequestParam(required = false) SortKey sort,
                            @RequestParam(required = false) String q,
                            Model model) {
        LocalDate today = LocalDate.now();
        SortKey effectiveSort = sort != null ? sort : SortKey.RENEWAL;
        Summary summary = service.summarize(today);

        model.addAttribute("subscriptions", service.search(category, effectiveSort, q, today));
        model.addAttribute("summary", summary);
        model.addAttribute("chartLabels", summary.byCategory().stream().map(c -> c.category().getLabel()).toList());
        model.addAttribute("chartValues", summary.byCategory().stream().map(c -> c.amount().longValue()).toList());
        model.addAttribute("today", today);
        model.addAttribute("reminderDays", SubscriptionService.REMINDER_DAYS);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedSort", effectiveSort);
        model.addAttribute("q", q == null ? "" : q);
        return "index";
    }

    @GetMapping("/subscriptions/new")
    public String newForm(Model model) {
        model.addAttribute("form", new SubscriptionForm());
        model.addAttribute("formAction", "/subscriptions");
        return "admin/subscription-edit";
    }

    @PostMapping("/subscriptions")
    public String create(@Valid @ModelAttribute("form") SubscriptionForm form,
                         BindingResult result, Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            model.addAttribute("formAction", "/subscriptions");
            return "admin/subscription-edit";
        }
        service.create(form);
        redirect.addFlashAttribute("message", "「" + form.getName() + "」を追加しました");
        return "redirect:/subscriptions";
    }

    @GetMapping("/subscriptions/{id}/edit")
    public String editForm(@PathVariable Long id, Model model, HttpServletResponse response) {
        Subscription s = service.find(id).orElse(null);
        if (s == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return "error/404";
        }
        model.addAttribute("form", SubscriptionForm.from(s));
        model.addAttribute("formAction", "/subscriptions/" + id);
        return "admin/subscription-edit";
    }

    @PostMapping("/subscriptions/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("form") SubscriptionForm form,
                         BindingResult result, Model model, RedirectAttributes redirect,
                         HttpServletResponse response) {
        if (result.hasErrors()) {
            model.addAttribute("formAction", "/subscriptions/" + id);
            return "admin/subscription-edit";
        }
        if (service.update(id, form).isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return "error/404";
        }
        redirect.addFlashAttribute("message", "「" + form.getName() + "」を更新しました");
        return "redirect:/subscriptions";
    }

    @PostMapping("/subscriptions/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect, HttpServletResponse response) {
        if (!service.delete(id)) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return "error/404";
        }
        redirect.addFlashAttribute("message", "削除しました");
        return "redirect:/subscriptions";
    }

    // フォーム・フィルタの選択肢(全ビュー共通)
    @ModelAttribute("categories")
    List<Category> categories() { return List.of(Category.values()); }

    @ModelAttribute("currencies")
    List<Currency> currencies() { return List.of(Currency.values()); }

    @ModelAttribute("billingCycles")
    List<BillingCycle> billingCycles() { return List.of(BillingCycle.values()); }

    @ModelAttribute("paymentStatuses")
    List<PaymentStatus> paymentStatuses() { return List.of(PaymentStatus.values()); }

    @ModelAttribute("sortKeys")
    List<SortKey> sortKeys() { return List.of(SortKey.values()); }
}
